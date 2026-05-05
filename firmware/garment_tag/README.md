# BLE Tag Firmware

## Overview

This folder contains the firmware for the custom BLE garment tag.

The firmware runs on the nRF52832-based custom PCB and broadcasts sensor data using BLE manufacturer-specific advertisements. The Raspberry Pi edge node reads these advertisements and uses the values for garment state detection.

---

## Main Firmware File

src/customPCB_firmware.c

---

## Platform

- Zephyr RTOS  
- nRF52832 (Raytac MDBT42Q BLE module)  
- nRF52 DK used for flashing/debugging via SWD  

---

## Sensors Used

- BMI270 (IMU / accelerometer)  
- SHTC3 (humidity and temperature sensor)  

---

## Functionality

The firmware:

- initializes I2C communication  
- reads acceleration data from the BMI270  
- estimates motion based on acceleration changes  
- reads humidity and temperature from the SHTC3  
- encodes the sensor values into a BLE advertisement payload  
- broadcasts the payload periodically  

---

## BLE Advertisement Payload

The firmware encodes sensor data into manufacturer-specific data.

Expected structure:

- Byte 0 → motion flag (0 or 1)  
- Byte 1 → humidity (%)  
- Byte 2 → temperature offset (temp + 40)  

The edge node parses this payload using the same byte positions.

Manufacturer ID:

1027

---

## Flashing

1. Connect the custom PCB to the nRF52 DK using SWD  
2. Build the firmware using Zephyr  
3. Flash the firmware onto the BLE module  
4. Power the board using a coin cell battery  
5. Verify BLE advertisements using the edge node or debug scripts  

---

## Notes

- The firmware broadcasts BLE advertisements continuously  
- The payload format must match what is expected in `pipeline_final.py`  
- Incorrect payload structure will break state detection  
- The device may advertise under a custom name (e.g. MyBeacon)  
