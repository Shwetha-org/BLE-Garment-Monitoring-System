#include <zephyr/kernel.h>
#include <zephyr/device.h>
#include <zephyr/drivers/i2c.h>
#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/bluetooth/hci.h>
#include <stdlib.h>

#include "bmi2.h"
#include "bmi270.h"

#define I2C_NODE DT_NODELABEL(i2c1)
#define BMI_ADDR 0x68
#define SHTC3_ADDR 0x70

static const struct device *i2c_dev = DEVICE_DT_GET(I2C_NODE);
static struct bmi2_dev bmi;

/* 03 04 = ID, then motion, humidity, temp+40 */
uint8_t adv_payload[5] = {0x03, 0x04, 0x00, 0x00, 0x00};

static struct bt_data ad[] = {
    BT_DATA(BT_DATA_NAME_COMPLETE, "MyBeacon", 8),
    BT_DATA(BT_DATA_MANUFACTURER_DATA, adv_payload, 5),
};

int8_t user_i2c_read(uint8_t reg_addr, uint8_t *data, uint32_t len, void *intf_ptr)
{
    return i2c_burst_read(i2c_dev, BMI_ADDR, reg_addr, data, len);
}

int8_t user_i2c_write(uint8_t reg_addr, const uint8_t *data, uint32_t len, void *intf_ptr)
{
    return i2c_burst_write(i2c_dev, BMI_ADDR, reg_addr, data, len);
}

void user_delay_us(uint32_t period, void *intf_ptr)
{
    k_usleep(period);
}

static int read_shtc3(uint8_t *humidity_percent, int8_t *temp_c)
{
    uint8_t wake[2] = {0x35, 0x17};
    uint8_t measure[2] = {0x7C, 0xA2};
    uint8_t sleep_cmd[2] = {0xB0, 0x98};
    uint8_t data[6];

    i2c_write(i2c_dev, wake, 2, SHTC3_ADDR);
    k_msleep(2);

    if (i2c_write(i2c_dev, measure, 2, SHTC3_ADDR) < 0) {
        return -1;
    }

    k_msleep(20);

    if (i2c_read(i2c_dev, data, 6, SHTC3_ADDR) < 0) {
        return -1;
    }

    uint16_t raw_t = ((uint16_t)data[0] << 8) | data[1];
    uint16_t raw_h = ((uint16_t)data[3] << 8) | data[4];

    float temp = -45.0f + 175.0f * ((float)raw_t / 65535.0f);
    float hum = 100.0f * ((float)raw_h / 65535.0f);

    if (hum < 0) hum = 0;
    if (hum > 100) hum = 100;

    *humidity_percent = (uint8_t)hum;
    *temp_c = (int8_t)temp;

    i2c_write(i2c_dev, sleep_cmd, 2, SHTC3_ADDR);

    return 0;
}

int main(void)
{
    struct bmi2_sens_config config;
    struct bmi2_sens_data sensor_data;
    int32_t prev_mag = 0;

    bt_enable(NULL);

    if (!device_is_ready(i2c_dev)) {
        while (1) k_msleep(1000);
    }

    bmi.read = user_i2c_read;
    bmi.write = user_i2c_write;
    bmi.delay_us = user_delay_us;
    bmi.intf = BMI2_I2C_INTF;

    bmi270_init(&bmi);

    config.type = BMI2_ACCEL;
    config.cfg.acc.odr = BMI2_ACC_ODR_100HZ;
    config.cfg.acc.range = BMI2_ACC_RANGE_2G;
    config.cfg.acc.bwp = BMI2_ACC_NORMAL_AVG4;
    config.cfg.acc.filter_perf = BMI2_PERF_OPT_MODE;

    bmi2_set_sensor_config(&config, 1, &bmi);

    uint8_t sens_list[1] = { BMI2_ACCEL };
    bmi2_sensor_enable(sens_list, 1, &bmi);

    bt_le_adv_start(BT_LE_ADV_NCONN, ad, ARRAY_SIZE(ad), NULL, 0);

    while (1) {
        if (bmi2_get_sensor_data(&sensor_data, &bmi) == BMI2_OK) {
            int32_t ax = sensor_data.acc.x;
            int32_t ay = sensor_data.acc.y;
            int32_t az = sensor_data.acc.z;

            int32_t mag = abs(ax) + abs(ay) + abs(az);
            int32_t diff = abs(mag - prev_mag);
            prev_mag = mag;

            adv_payload[2] = (diff > 500) ? 1 : 0;
        }

        uint8_t humidity;
        int8_t temp;

        if (read_shtc3(&humidity, &temp) == 0) {
            adv_payload[3] = humidity;
            adv_payload[4] = (uint8_t)(temp + 40);
        }

        bt_le_adv_update_data(ad, ARRAY_SIZE(ad), NULL, 0);
        k_msleep(1000);
    }
}