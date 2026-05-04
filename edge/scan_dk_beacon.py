import asyncio
from datetime import datetime
from bleak import BleakScanner

#nordic semiconductor company ID from the beacon advertising data
COMPANY_ID = 0x0059

#ID byte we gave this DK beacon in the firmware
EXPECTED_ID = 0x01


def detection_callback(device, adv_data):
    #get manufacturer specific data from this advertisement
    mfg = adv_data.manufacturer_data or {}
    if COMPANY_ID not in mfg:
        return

    data = mfg[COMPANY_ID]
    if len(data) < 2:
        return

    #first byte = beacon ID, second byte = status/battery
    dev_id = data[0]
    status = data[1]

    #only show lines for our DK beacon
    if dev_id != EXPECTED_ID:
        return

    ts = datetime.now().isoformat(timespec="seconds")
    rssi = adv_data.rssi
    print(f"[{ts}] DK {device.address} RSSI={rssi} ID=0x{dev_id:02X} STATUS=0x{status:02X}")


async def main():
    #start BLE scan and call detection_callback for each packet
    scanner = BleakScanner(detection_callback)
    await scanner.start()
    print("Scanning for DK beacon... Press Ctrl+C to stop.")
    try:
        while True:
            await asyncio.sleep(1)
    finally:
        await scanner.stop()


if __name__ == "__main__":
    asyncio.run(main())
