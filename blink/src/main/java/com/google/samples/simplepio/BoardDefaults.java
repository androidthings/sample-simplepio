package com.google.samples.simplepio;

import android.os.Build;

@SuppressWarnings("WeakerAccess")
public class BoardDefaults {
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_NXP = "nxp";

    /**
     * Return the GPIO pin that the LED is connected on.
     * For example, on Intel Edison Arduino breakout, pin "IO13" is connected to an onboard LED
     * that turns on when the GPIO pin is HIGH, and off when low.
     */
    public static String getGPIOForLED() {
        // TODO: confirm DEVICE and preferred port for RPI3 and NXP
        switch (Build.DEVICE) {
            case DEVICE_EDISON:
                return "IO13";
            case DEVICE_RPI3:
                return "26";
            case DEVICE_NXP:
                return "26";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
