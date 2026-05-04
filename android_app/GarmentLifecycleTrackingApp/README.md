# Android Application

## Overview

This Android application is the mobile user interface for the BLE Garment Monitoring System.

The app is built using Kotlin and Jetpack Compose. It connects to the Raspberry Pi edge node through a REST API and displays the latest garment usage summary.

---

## Main File

The main application logic is implemented in:

app/src/main/java/com/example/garmentlifecycletrackingapp/MainActivity.kt

This file contains:

- Jetpack Compose UI  
- Retrofit API connection  
- garment state display  
- wear and wash counters  
- connection status indicator  
- privacy settings dialog  

---

## Functionality

The app displays:

- current garment state  
- wear count  
- wash count  
- connection status  
- privacy settings  

The UI refreshes automatically every 2 seconds while data collection is enabled.

---

## API Connection

The app connects to the Raspberry Pi Flask API:

http://<raspberry-pi-ip>:5000/summary

In `MainActivity.kt`, the current base URL is set here:

.baseUrl("http://172.20.10.12:5000/")

When running the system on a different network, replace this IP address with the Raspberry Pi IP address.

---

## Expected API Response

The app expects a JSON response in this format:

{
  "device_id": "0x0403",
  "state": "worn",
  "wears": 2,
  "washes": 1
}

Only these fields are currently used by the Android app:

- device_id  
- state  
- wears  
- washes  

---

## Technologies Used

- Kotlin  
- Jetpack Compose  
- Material 3  
- Retrofit  
- Gson converter  
- SharedPreferences  

---

## How to Run

1. Open android_app/GarmentLifecycleTrackingApp/ in Android Studio  
2. Make sure the Raspberry Pi edge node is running  
3. Update the API base URL in MainActivity.kt  
4. Build and run the app on an Android device or emulator  

---

## Notes

- The Raspberry Pi and Android device must be on the same local network  
- The Flask API must be running before the app can display live data  
- If the app cannot connect to the API, it shows an offline/error state  
- Privacy settings are stored locally using SharedPreferences  