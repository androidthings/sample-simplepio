package com.google.samples.simplepio;

import android.os.Build;

@SuppressWarnings("WeakerAccess")
public class BoardDefaults {
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_NXP = "nxp";

    /**
     * Return the preferred PWM port for each board.
     */
    public static String getPWMPort() {
        // TODO: confirm DEVICE and preferred port for RPI3 and NXP
        switch (Build.DEVICE) {
            case DEVICE_EDISON:
                return "IO6";
            case DEVICE_RPI3:
                return "PWM0";
            case DEVICE_NXP:
                return "26";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}