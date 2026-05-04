# Setup Guide

## 1. Clone the Repository

```bash
git clone https://github.com/Shwetha-org/BLE-Garment-Monitoring-System.git
cd BLE-Garment-Monitoring-System
```

---

## 2. Required Hardware

* Custom BLE garment tag
* Raspberry Pi with Bluetooth enabled
* nRF52 DK for flashing/debugging the custom tag
* Coin cell battery for the garment tag
* Android device or emulator

---

## 3. Install Edge Dependencies

```bash
cd edge
pip install -r requirements.txt
```

---

## 4. Run the Edge Node

Run from inside the `edge/` folder:

```bash
python pipeline_final.py
```

The edge node will:

* scan BLE advertisements
* classify garment state
* store readings in SQLite
* start the Flask API

---

## 5. Verify Database Creation

After running the edge node, the database is created at:

```text
edge/data/garment_data.db
```

This file is generated at runtime and is not committed to GitHub.

---

## 6. Verify API

Open this in a browser or use curl:

```text
http://<raspberry-pi-ip>:5000/summary
```

Expected output format:

```json
{
  "device_id": "0x0403",
  "state": "stored",
  "room_state": "in-room",
  "movement_state": "stationary",
  "wears": 0,
  "washes": 0,
  "humidity": 0,
  "gyro_mag": 0,
  "rssi": -65,
  "rssi_stable": true,
  "wash_score": 0
}
```

---

## 7. Optional BLE Debug Scan

To test BLE advertising separately:

```bash
python scan_dk_beacon.py
```

This script is used to verify BLE advertisement transmission and RSSI before running the full pipeline.

---

## 8. Android App Setup

1. Open `android_app/` in Android Studio.
2. Set the API base URL to:

```text
http://<raspberry-pi-ip>:5000
```

3. Build and run the app.
4. Confirm that the app displays the garment state returned by `/summary`.

---

## 9. Reproduction Checklist

A successful setup should have:

* BLE garment tag powered and advertising
* Raspberry Pi scanning BLE packets
* `pipeline_final.py` running without errors
* SQLite database created under `edge/data/`
* `/summary` API returning JSON
* Android app displaying the summary

---
