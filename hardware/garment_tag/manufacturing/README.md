# BBGT BLE Garment Tag – PCB Outputs

This folder contains the manufacturing outputs for the **BLE garment tag** (BBGT) used in the circular fashion garment tracking system, revision 1.

## Files

- `ble-beacon-*.gbr` – Gerber files for all copper, mask, paste, silkscreen and edge cuts layers.  
- `ble-beacon-*.drl` – Drill files for plated and non‑plated holes.  
- `ble-beacon-job.gbrjob` – KiCad fabrication job file (optional for fab).  
- `tag_final_bom.csv` – Bill of materials for assembly.

You can zip this folder and upload it directly to any standard PCB manufacturer that supports KiCad/RS‑274X Gerbers.

## Electrical and programming

- Supply voltage: 3 V from CR2032 coin cell.  
- MCU/Module: nRF52832 BLE module.  
- Programming: 2×5 1.27 mm SWD header.

SWD header pinout (top view, notch on the left):

1. VCC (3 V)  
2. SWDIO  
3. SWCLK  
4. GND  
5. RESET

## Layout and assembly notes

- Keep‑out: Do **not** place copper, ground pour, or tall components under the RF module antenna region on any layer.  
- Place the tag away from large metal surfaces for best RF performance.  
- IMU and additional sensors are optional for basic BLE advertising functionality.
