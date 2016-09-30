package com.google.samples.simplepio;

// Compensation formula from the BMP280 datasheet.
// https://cdn-shop.adafruit.com/datasheets/BST-BMP280-DS001-11.pdf
public class Bmp280Calibration {
    public static double compensateTemperature(int rawTemp) {
        // Compensate temperature according to calibration data in the datasheet example.
        double adc_T = rawTemp;
        int dig_T1 = 27504;
        int dig_T2 = 26435;
        int dig_T3 = -1000;
        double var1 = (adc_T / 16384.0 - dig_T1 / 1024.0) * dig_T2;
        double var2 = ((adc_T / 131072.0 - dig_T1 / 8192.0) *
            (adc_T / 131072.0 - dig_T1 / 8192.0)) * dig_T3;
        return (var1 + var2) / 5120.0;
    }
}