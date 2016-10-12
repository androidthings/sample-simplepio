package com.google.samples.simplepio;

import android.app.Activity;
import android.hardware.pio.I2cDevice;
import android.hardware.pio.PeripheralManagerService;
import android.os.Bundle;
import android.os.Handler;
import android.system.ErrnoException;
import android.util.Log;

/**
 * TemperatureActivity is an example that use the Peripheral IO
 * to read values from a I2C BMP280 temperature sensor.
 */
public class TemperatureActivity extends Activity {
    private static final String TAG = "TemperatureActivity";
    /**
     * I2C address of the temperature sensor.
     */
    private static final int SENSOR_I2C_ADDR = 0x77;
    /**
     * Interval in milliseconds between each sensor reading.
     */
    private static final int INTERVAL_BETWEEN_SENSOR_READ_MS = 1000;

    private static final int BMP280_REG_ID = 0xD0;
    private static final int BMP280_REG_CTRL = 0xF4;
    private static final int BMP280_REG_TEMP = 0xFA;
    private static final int BMP280_POWER_MODE_NORMAL           = 0b00000011;
    private static final int BMP280_OVERSAMPLING_TEMP_1X         = 0b00100000;

    private PeripheralManagerService mService;
    private Handler mHandler = new Handler();
    private Runnable mSensorRunnable = this::readSensorValue;
    private I2cDevice mSensorDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting TemperatureActivity");

        mService = new PeripheralManagerService();
        try {
            // Open the I2C bus.
            String pinName = BoardDefaults.getI2CPort();
            Log.d(TAG, "Opening I2C device for the sensor: " + pinName);
            mSensorDevice = mService.openI2cDevice(pinName, SENSOR_I2C_ADDR);
            // Read sensor chip id.
            int chipId = mSensorDevice.readRegByte(BMP280_REG_ID);
            Log.d(TAG, "Sensor chip id: " + chipId);
            // Configure Sensor temperature reading.
            // Power mode: Normal
            // Temperature resolution: 16bit
            // Pressure resolution: skip
            mSensorDevice.writeRegByte(BMP280_REG_CTRL,
                    (byte) (BMP280_POWER_MODE_NORMAL | BMP280_OVERSAMPLING_TEMP_1X));
            Log.d(TAG, "Start reading temperature values from the sensor");
            // Post a Runnable that continously read the sensor value.
            mHandler.post(mSensorRunnable);
        } catch (ErrnoException e) {
            Log.e(TAG, "Error configuring I2C device", e);
        }
    }

    @Override
    protected void onDestroy(){
        // Remove pending sensor Runnable from the handler.
        mHandler.removeCallbacks(mSensorRunnable);
        // Close the I2C device.
        Log.d(TAG, "Closing Sensor device");
        mSensorDevice.close();
        mSensorDevice = null;
    }

    private void readSensorValue() {
        // Exit Runnable if the I2C device is already closed.
        if (mSensorDevice == null) {
            return;
        }
        try {
            // Read raw temperature registers.
            byte[] buf = new byte[3];
            mSensorDevice.readRegBuffer(BMP280_REG_TEMP, buf, 3);
            // msb[7:0] lsb[7:0] xlsb[7:4]
            int msb = buf[0] & 0xff;
            int lsb = buf[1] & 0xff;
            int xlsb = buf[2] & 0xf0;
            // Convert to 20bit integer
            int rawTemp = (msb << 16 | lsb << 8 | xlsb) >> 4;
            // Compensate temperature using calibration data.
            double tempValue = Bmp280Calibration.compensateTemperature(rawTemp);
            Log.d(TAG, "temperature: " + tempValue);
            mHandler.postDelayed(mSensorRunnable, INTERVAL_BETWEEN_SENSOR_READ_MS);
        } catch (ErrnoException e) {
            Log.e(TAG, "Error reading sensor value", e);
        }
    }

 }
