package com.google.samples.simplepio;

import android.app.Activity;
import android.hardware.pio.I2cDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.hardware.pio.PeripheralManagerService;
import android.system.ErrnoException;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * TemperatureActivity is an example that use the Peripheral IO
 * to read values from a I2C BMP280 temperature sensor.
 */
public class TemperatureActivity extends Activity {
    private static final String TAG = "BlinkActivity";
    /**
     * I2C bus the temperature sensor is connected to.
     */
    private static final String SENSOR_I2C_BUS = "I2C1";
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
    private static final int BMP280_REG_TEMP_CALIB_1 = 0x88;
    private static final int BMP280_REG_TEMP_CALIB_2 = 0x8A;
    private static final int BMP280_REG_TEMP_CALIB_3 = 0x8C;
    private static final int BMP280_POWER_MODE_NORMAL           = 0b00000011;
    private static final int BMP280_OVERSAMPLING_PRESSURE_SKIP  = 0b00000000;
    private static final int BMP280_OVERSAMPLING_TEMP_1X         = 0b00100000;

    private PeripheralManagerService mService;
    private Handler mHandler = new Handler();
    private Runnable mSensorRunnable = this::readSensorValue;
    private I2cDevice mSensorDevice;
    private int[] mCalibrationData = new int[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting TemperatureActivity");

        mService = new PeripheralManagerService();
        try {
            // List available I2C buses:
            List<String> busList = mService.getI2CBusList();
            if (busList == null || busList.isEmpty()) {
                Log.e(TAG, "No I2C bus available on this device.");
                return;
            }
            Log.i(TAG, "List of available I2C buses: " + busList);
            if (!busList.contains(SENSOR_I2C_BUS)) {
                Log.e(TAG, "Sensor I2C bus not found: " + SENSOR_I2C_BUS);
                return;
            }
            // Open the I2C bus.
            Log.i(TAG, "Opening I2C device for the sensor: " + SENSOR_I2C_BUS);
            mSensorDevice = mService.openI2cDevice(SENSOR_I2C_BUS, SENSOR_I2C_ADDR);
            // Read sensor chip id.
            int chipId = mSensorDevice.readRegByte(BMP280_REG_ID);
            Log.i(TAG, "Sensor chip id: " + chipId);
            // Read unsigned short temp calibration data 1.
            mCalibrationData[0] = mSensorDevice.readRegWord(BMP280_REG_TEMP_CALIB_1);
            // Read signed short temp calibration data 2.
            mCalibrationData[1] = (short)mSensorDevice.readRegWord(BMP280_REG_TEMP_CALIB_2);
            // Read signed short temp calibration data 3.
            mCalibrationData[2] = (short)mSensorDevice.readRegWord(BMP280_REG_TEMP_CALIB_3);
            Log.i(TAG, "Calibration data: " + mCalibrationData[0]
                                            + ", " + mCalibrationData[1]
                                            + ", " + mCalibrationData[2]);
            // Configure Sensor temperature reading.
            // Power mode: Normal
            // Temperature resolution: 16bit
            // Pressure resolution: skip
            mSensorDevice.writeRegByte(BMP280_REG_CTRL,BMP280_POWER_MODE_NORMAL
                            |BMP280_OVERSAMPLING_TEMP_1X
                            |BMP280_OVERSAMPLING_PRESSURE_SKIP);
            Log.i(TAG, "Start reading temperature values from the sensor");
            // Post a Runnable that continously read the sensor value.
            mHandler.post(mSensorRunnable);
        } catch (RemoteException | ErrnoException e) {
            Log.e(TAG, "Error configuring I2C device", e);
        }
    }

    @Override
    protected void onDestroy(){
        // Remove pending sensor Runnable from the handler.
        mHandler.removeCallbacks(mSensorRunnable);
        // Close the I2C device.
        Log.i(TAG, "Closing Sensor device");
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
            byte[] regBytes = mSensorDevice.readRegBuffer(BMP280_REG_TEMP, 3);
            // msb[7:0] lsb[7:0] xlsb[7:4]
            int msb = regBytes[0] & 0xff;
            int lsb = regBytes[1] & 0xff;
            int xlsb = regBytes[2] & 0xf0;
            // Convert to 20bit integer
            int rawTemp = (msb << 16 | lsb << 8 | xlsb) >> 4;
            // Compensate temperature using calibration data.
            double tempValue = Bmp280Calibration.compensateTemperature(rawTemp, mCalibrationData);
            Log.i(TAG, "temperatureDouble: " + tempValue);
            mHandler.postDelayed(mSensorRunnable, INTERVAL_BETWEEN_SENSOR_READ_MS);
        } catch (RemoteException | ErrnoException e) {
            Log.e(TAG, "Error reading sensor value", e);
        }
    }

 }
