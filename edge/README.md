# Edge Node (Raspberry Pi)

## Overview

This component runs on the Raspberry Pi and is the core of the system.
It performs BLE scanning, signal processing, garment state classification, local data storage, and exposes a REST API for the Android application.

---

## Responsibilities

The edge node is responsible for:

* Scanning BLE advertisements from the garment tag
* Extracting sensor data (motion, humidity, temperature, gyro, RSSI)
* Applying filtering and aggregation using sliding windows
* Detecting garment states:

  * stored
  * worn
  * washed
* Storing processed data in a local SQLite database
* Exposing the latest state via a Flask API (`/summary`)

---

## Files

- `pipeline_final.py`  
  Main system used on the Raspberry Pi.  
  It scans BLE advertisements, processes sensor data, performs garment state classification, stores results in a SQLite database, and exposes a REST API.

- `scan_dk_beacon.py`  
  Debug script used with the nRF52 DK beacon.  
  It scans BLE advertisements, filters manufacturer-specific data from Nordic Semiconductor devices, and prints device ID, status, and RSSI to verify correct beacon transmission.

- `requirements.txt`  
  Dependency file for the edge node.  
  It lists the Python libraries required to run the BLE scanner, processing pipeline, and API server.

- `data/`  
  Runtime directory used by the edge node.  
  It stores the SQLite database (`garment_data.db`) that is automatically created and updated during system execution.

---

## Requirements

* Python 3.9+
* Raspberry Pi with Bluetooth enabled
* Linux environment (tested on Raspberry Pi OS)

---

## Installation

Install dependencies:

```bash
pip install -r requirements.txt
```

---

## Running the System

IMPORTANT: You must run the script from inside the `edge/` folder.

```bash
cd edge
python pipeline_final.py
```

---

## Output

### 1. SQLite Database

The system creates a database at:

```
edge/data/garment_data.db
```

This file is generated automatically at runtime and is not included in the repository.

---

### 2. Console Logs

Example output:

```
in-room | moving | state=worn | wears=2 | washes=1 | gyro_mag=4200.0 | rssi=-65 | rssi_stable=True | wash_candidate=False | wash_score=0.20
```

---

### 3. API Endpoint

The Flask server runs on:

```
http://<raspberry-pi-ip>:5000/summary
```

Example response:

```json
{
  "device_id": "0x0403",
  "state": "worn",
  "room_state": "in-room",
  "movement_state": "moving",
  "wears": 2,
  "washes": 1,
  "humidity": 45.2,
  "gyro_mag": 4100,
  "rssi": -65,
  "rssi_stable": true,
  "wash_score": 0.2
}
```

---

## State Detection Logic (Summary)

The system uses lightweight edge processing:

* Movement detection:
  Based on averaged motion values

* Room detection:
  Based on RSSI threshold

* Wash detection:
  Based on:

  * high gyro magnitude
  * stable RSSI
  * rolling wash score

* Wear detection:
  Based on sustained movement over a time window

---

## Important Notes

* The database path is relative to the current working directory
  → Always run the script from `edge/`

* The database file is created automatically
  → Do not manually add it to the repository

* BLE data is filtered to remove invalid samples before processing

---

## Debugging

To test BLE reception only:

```bash
python scan_dk_beacon.py
```

This prints raw beacon RSSI and ID values.

---

## Limitations

* Designed for a single garment device (`device_id = 0x0403`)
* Uses simple threshold-based classification
* Not optimized for multi-device environments

---
