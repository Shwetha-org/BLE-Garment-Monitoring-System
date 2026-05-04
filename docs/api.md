# API

## Overview

The edge node exposes a REST API over HTTP.
The Android application uses this API to retrieve the latest garment state.

Base URL:

```text
http://<raspberry-pi-ip>:5000
```

---

## Endpoint

### GET /summary

Returns the latest processed garment data.

---

## Request

```http
GET /summary HTTP/1.1
Host: <raspberry-pi-ip>:5000
```

No parameters required.

---

## Response

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

## Field Descriptions

* `device_id`
  Identifier of the BLE garment tag.

* `state`
  Current garment state: `stored`, `worn`, or `washed`.

* `room_state`
  Indicates whether the garment is detected as in-room or out-of-room based on RSSI.

* `movement_state`
  Indicates whether movement is detected (`moving` or `stationary`).

* `wears`
  Number of detected wear sessions.

* `washes`
  Number of detected wash events.

* `humidity`
  Averaged humidity value from sensor readings.

* `gyro_mag`
  Magnitude of gyroscope data used for motion analysis.

* `rssi`
  Received Signal Strength Indicator from BLE advertisement.

* `rssi_stable`
  Boolean indicating stability of RSSI values.

* `wash_score`
  Rolling score used to confirm wash detection.

---

## Usage Notes

* The API always returns the latest processed state.
* No authentication is required.
* The endpoint is intended for local network use.

---

## Error Handling

If the edge node is not running, the endpoint will not respond.

Ensure:

* `pipeline_final.py` is running
* Raspberry Pi is connected to the network
* correct IP address is used

---
