import asyncio, time, sqlite3, threading, os
from collections import deque
from flask import Flask, jsonify
from bleak import BleakScanner

DB_NAME = "data/garment_data.db"
TARGET_COMPANY_ID = 1027

WINDOW = 5
GYRO_WINDOW = 10
RSSI_WINDOW = 8

RSSI_THRESHOLD = -80
RSSI_STABLE_THRESHOLD = 8

WORN_CONFIRM_SECONDS = 10
WEAR_RESET_SECONDS = 20
WASH_RESET_SECONDS = 20

GYRO_THRESHOLD = 4000
WASH_SCORE_THRESHOLD = 0.5

motion_window = deque(maxlen=WINDOW)
humidity_window = deque(maxlen=WINDOW)
gyro_window = deque(maxlen=GYRO_WINDOW)
rssi_window = deque(maxlen=RSSI_WINDOW)
wash_candidate_window = deque(maxlen=20)

wear_count = 0
wash_count = 0
current_state = "stored"

worn_start_time = None
stored_start_time = None

wear_session_active = False
wash_session_active = False

db_lock = threading.Lock()

latest_summary = {
    "device_id": "0x0403",
    "state": "stored",
    "room_state": "unknown",
    "movement_state": "stationary",
    "wears": 0,
    "washes": 0,
    "humidity": 0,
    "gyro_mag": 0,
    "rssi": 0,
    "rssi_stable": False,
    "wash_score": 0
}

app = Flask(__name__)

def init_db():
    os.makedirs("data", exist_ok=True)
    
    with db_lock:
        conn = sqlite3.connect(DB_NAME, timeout=30)
        c = conn.cursor()
        c.execute("""
        CREATE TABLE IF NOT EXISTS beacon_readings (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp TEXT,
            device_id TEXT,
            rssi INTEGER,
            motion INTEGER,
            humidity REAL,
            temp_c INTEGER,
            room_state TEXT,
            movement_state TEXT,
            state TEXT
        )
        """)
        conn.commit()
        conn.close()

def save_reading(device_id, rssi, motion, humidity, temp_c, room_state, movement_state, state):
    with db_lock:
        conn = sqlite3.connect(DB_NAME, timeout=30)
        c = conn.cursor()
        timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
        c.execute("""
        INSERT INTO beacon_readings
        (timestamp, device_id, rssi, motion, humidity, temp_c, room_state, movement_state, state)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (timestamp, device_id, rssi, motion, humidity, temp_c, room_state, movement_state, state))
        conn.commit()
        conn.close()

def avg(values):
    return sum(values) / len(values)

def is_valid_sample(rssi, motion, humidity, temp_c):
    return (
        rssi is not None and
        -100 <= rssi <= 0 and
        motion in [0, 1] and
        0 <= humidity <= 100 and
        -10 <= temp_c <= 60
    )

def detection_callback(device, advertisement_data):
    global wear_count, wash_count, current_state
    global worn_start_time, stored_start_time
    global wear_session_active, wash_session_active

    mfg = advertisement_data.manufacturer_data
    if TARGET_COMPANY_ID not in mfg:
        return

    payload = mfg[TARGET_COMPANY_ID]

    if len(payload) < 15:
        return

    motion = payload[0]
    humidity = payload[1]
    temp_c = payload[2] - 40

    gx = int.from_bytes(payload[9:11], "little", signed=True)
    gy = int.from_bytes(payload[11:13], "little", signed=True)
    gz = int.from_bytes(payload[13:15], "little", signed=True)

    rssi = advertisement_data.rssi

    if not is_valid_sample(rssi, motion, humidity, temp_c):
        return

    room_state = "in-room" if rssi > RSSI_THRESHOLD else "out-of-room"

    motion_window.append(motion)
    humidity_window.append(humidity)
    rssi_window.append(rssi)

    gyro_mag = abs(gx) + abs(gy) + abs(gz)
    gyro_window.append(gyro_mag)

    if len(motion_window) < WINDOW or len(gyro_window) < GYRO_WINDOW or len(rssi_window) < RSSI_WINDOW:
        return

    now = time.time()

    humidity_avg = avg(humidity_window)
    motion_avg = avg(motion_window)

    movement_detected = motion_avg >= 0.2
    movement_state = "moving" if movement_detected else "stationary"

    rssi_variation = max(rssi_window) - min(rssi_window)
    rssi_stable = rssi_variation <= RSSI_STABLE_THRESHOLD

    wash_candidate = gyro_mag >= GYRO_THRESHOLD and rssi_stable

    wash_candidate_window.append(wash_candidate)
    wash_score = sum(wash_candidate_window) / len(wash_candidate_window)

    if wash_score >= WASH_SCORE_THRESHOLD:
        current_state = "washed"
        worn_start_time = None
        stored_start_time = None
        wear_session_active = True

        if not wash_session_active:
            wash_count += 1
            wash_session_active = True

    elif movement_detected and not wash_session_active:
        if not wash_candidate:
            wash_candidate_window.clear()

        if worn_start_time is None:
            worn_start_time = now

        stored_start_time = None

        if now - worn_start_time >= WORN_CONFIRM_SECONDS:
            current_state = "worn"

            if not wear_session_active:
                wear_count += 1
                wear_session_active = True
        else:
            current_state = "stored"

    else:
        if stored_start_time is None:
            stored_start_time = now

        worn_start_time = None

        if now - stored_start_time >= WEAR_RESET_SECONDS:
            current_state = "stored"
            wear_session_active = False

        if now - stored_start_time >= WASH_RESET_SECONDS:
            wash_session_active = False
            wash_candidate_window.clear()

    latest_summary.update({
        "device_id": "0x0403",
        "state": current_state,
        "room_state": room_state,
        "movement_state": movement_state,
        "wears": wear_count,
        "washes": wash_count,
        "humidity": round(humidity_avg, 1),
        "gyro_mag": round(gyro_mag, 1),
        "rssi": rssi,
        "rssi_stable": rssi_stable,
        "wash_score": round(wash_score, 2)
    })

    save_reading("0x0403", rssi, motion, round(humidity_avg, 1), temp_c, room_state, movement_state, current_state)

    print(
        f"{room_state} | {movement_state} | state={current_state} | "
        f"wears={wear_count} | washes={wash_count} | "
        f"gyro_mag={gyro_mag:.1f} | rssi={rssi} | "
        f"rssi_stable={rssi_stable} | wash_candidate={wash_candidate} | "
        f"wash_score={wash_score:.2f}"
    )

@app.route("/summary")
def summary():
    return jsonify(latest_summary)

async def ble_loop():
    scanner = BleakScanner(detection_callback=detection_callback)
    await scanner.start()
    print("BLE scanner + gyro/RSSI wash classifier running...")

    while True:
        await asyncio.sleep(1)

def start_flask():
    app.run(host="0.0.0.0", port=5000)

if __name__ == "__main__":
    init_db()

    flask_thread = threading.Thread(target=start_flask)
    flask_thread.daemon = True
    flask_thread.start()

    asyncio.run(ble_loop())
