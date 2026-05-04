# Garment Tag Hardware

## Overview

This folder contains the hardware design files for the custom BLE garment tag used in the garment monitoring system.

The garment tag is a custom-built embedded device that integrates a BLE module and sensors on a single PCB. It continuously broadcasts sensor data via BLE advertisements, which are received and processed by the Raspberry Pi edge node.

---

## Folder Structure

* `kicad/`
  Contains the KiCad schematic and PCB design files.

* `assembly/`
  Contains reference images of the assembled PCB.

* `manufacturing/`
  Contains Gerber files, drill files, and BOM used for PCB fabrication.

---

## Main Hardware Components

* Raytac MDBT42Q (nRF52832 BLE module used for BLE advertising)
* IMU sensor (used for motion and gyroscope data)
* Humidity sensor
* Coin cell battery (power supply)
* Custom PCB integrating all components into a wearable garment tag

The custom PCB integrates the BLE module and sensors into a single wearable device that continuously broadcasts environmental and motion data via BLE advertisements.

---

## KiCad Design Files

The PCB and schematic can be opened using KiCad:

```text
hardware/garment_tag/kicad/
```

Key files:

```text
ble beacon.kicad_pcb
ble beacon.kicad_sch
ble beacon.kicad_pro
```

---

## Manufacturing Files

Manufacturing files are located in:

```text
hardware/garment_tag/manufacturing/
```

These include:

* Copper layers
* Solder mask layers
* Silkscreen layers
* Drill files
* BOM (bill of materials) CSV

These files can be directly used for PCB fabrication.

---

## Assembly Reference

Assembly images are located in:

```text
hardware/garment_tag/assembly/
```

These show:

* Front of PCB
* Back of PCB
* Close-up component placement

They can be used as reference when assembling the board.

---

## Reproduction Steps

1. Open the KiCad project from `kicad/`.
2. Review the schematic and PCB layout.
3. Use the files in `manufacturing/` to fabricate the PCB.
4. Use the BOM to source components.
5. Assemble the PCB using the reference images in `assembly/`.
6. Flash firmware onto the BLE module using the nRF52 DK via SWD.
7. Power the board using a coin cell battery.
8. Verify that the device broadcasts BLE advertisement data.

---

## Notes

* The nRF52 DK is used only as a development and flashing tool.
* The custom garment tag PCB is the actual BLE device used in the system.
* The BLE advertisements from this device are processed by the edge node (`pipeline_final.py`).

---
