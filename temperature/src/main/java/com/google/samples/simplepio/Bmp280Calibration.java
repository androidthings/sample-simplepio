package com.google.samples.simplepio;


import java.nio.ByteBuffer;

// Compensation formula from the BMP280 datasheet.
// https://cdn-shop.adafruit.com/datasheets/BST-BMP280-DS001-11.pdf
public class Bmp280Calibration {
    public static double compensateTemperature(int rawTemp, int[] calibrationData) {
        // Compensate temperature according to calibration.
        double adc_T = rawTemp;
        int dig_T1 = calibrationData[0];
        int dig_T2 = calibrationData[1];
        int dig_T3 = calibrationData[2];
        double var1 = (adc_T / 16384.0 - dig_T1 / 1024.0) * dig_T2;
        double var2 = ((adc_T / 131072.0 - dig_T1 / 8192.0) *
            (adc_T / 131072.0 - dig_T1 / 8192.0)) * dig_T3;
        return (var1 + var2) / 5120.0;
    }
}