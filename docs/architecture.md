# System Architecture

## Overview

The BLE Garment Monitoring System is an IoT-based system that monitors garment usage using a custom BLE garment tag, a Raspberry Pi edge node, local storage, and an Android application.

The system detects basic garment states:

* stored
* worn
* washed

---

## High-Level Architecture

```text
[Custom BLE Garment Tag]
          |
          | BLE advertisements
          v
[Raspberry Pi Edge Node]
          |
          | processing + classification
          v
[SQLite Database + Flask API]
          |
          | HTTP GET /summary
          v
[Android App]
```

---

## Components

### 1. Custom BLE Garment Tag

The garment tag is a custom PCB attached to a garment. It includes:

* Raytac MDBT42Q (nRF52832 BLE module)
* IMU sensor (motion + gyroscope)
* humidity sensor
* coin cell battery

The tag broadcasts sensor data using BLE advertisements.

Hardware files:

```text
hardware/garment_tag/
```

---

### 2. Raspberry Pi Edge Node

The Raspberry Pi acts as the edge-processing unit.

It is responsible for:

* scanning BLE advertisements
* extracting sensor data
* applying filtering and aggregation
* classifying garment state
* storing readings in SQLite
* exposing a REST API

Edge software:

```text
edge/
```

---

### 3. Local SQLite Database

The SQLite database stores processed BLE readings locally.

It is created at runtime and not included in the repository.

Expected location:

```text
edge/data/garment_data.db
```

---

### 4. Flask API

The edge node exposes:

```text
GET /summary
```

This endpoint returns the latest garment state and summary data.

---

### 5. Android Application

The Android app connects to the Raspberry Pi and displays garment state and usage.

API endpoint:

```text
http://<raspberry-pi-ip>:5000/summary
```

---

## Data Flow

1. The garment tag broadcasts sensor data via BLE.
2. The Raspberry Pi scans BLE advertisements.
3. Data is validated and filtered.
4. RSSI, motion, humidity, temperature, and gyro values are processed.
5. The system classifies the garment as stored, worn, or washed.
6. The result is stored in SQLite.
7. The latest summary is exposed via the Flask API.
8. The Android app fetches and displays the data.

---

## Design Decisions

### Edge Processing

Processing is performed locally on the Raspberry Pi.

Reasons:

* reduces data transmission
* keeps raw data local
* supports privacy
* enables offline operation

---

### SQLite Storage

SQLite is used because it is lightweight and suitable for local storage on the Raspberry Pi.

---

### Flask API

Flask provides a simple REST interface for communication with the Android application.

---

### BLE Advertisements

BLE advertisements are used instead of connections to allow low-power broadcasting without maintaining a connection.

---

## State Detection Summary

The system uses threshold-based logic and sliding windows:

* RSSI threshold → in-room vs out-of-room
* motion window → movement detection
* gyro magnitude → wash detection
* RSSI stability → distinguishes washing from movement
* wash score → confirms washing over time

---

## Current Limitations

* Supports a single garment tag
* Uses threshold-based classification (not ML-based)
* Requires tuning for different environments
* Database is local to the Raspberry Pi

---
