# BLE Garment Monitoring System

This project implements a BLE-based garment monitoring system for circular fashion services. It uses battery-powered BLE garment tags and a Raspberry Pi edge node to infer coarse garment states such as stored, worn, and in-laundry, and is designed to be compared against an RFID-based pipeline in a parallel study. [memory:336][memory:337]

## Repository structure

- `hardware/garment_tag/`
  - `ble beacon/` – KiCad project for the BLE garment tag (BBGT) PCB, based on an nRF52832 module with optional motion and environmental sensors. Edit the schematic and layout here. [memory:331][memory:333]
  - `outputs/` – Manufacturing outputs for the current BLE garment tag revision (Gerbers, drill files, fabrication job, BOM, and a short README for PCB fabs).

- `rpi_edge/` (planned) – Code and configuration for the Raspberry Pi edge node that scans BLE advertisements, logs data, and runs lightweight signal processing for garment state detection. [memory:336][memory:337]

- `docs/` – Reports, figures, and notes related to the system design and experiments.

The system is developed as part of a comparative study of BLE and RFID for garment lifecycle monitoring in circular fashion, with a focus on edge processing, accuracy, and sustainability trade-offs. [memory:336][memory:337]