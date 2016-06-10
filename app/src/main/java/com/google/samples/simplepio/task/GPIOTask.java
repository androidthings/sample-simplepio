package com.google.samples.simplepio.task;

import android.os.RemoteException;
import android.pio.Gpio;
import android.pio.PeripheralManagerService;
import android.system.ErrnoException;
import android.util.Log;

import com.google.samples.simplepio.SimplePIOService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Simple GPIO port handling.
 */

public class GPIOTask implements Runnable {
    private static final String TAG = SimplePIOService.TAG;

    private PeripheralManagerService mService;

    /**
     * Preferred GPIO port to look for. For example, on Intel Edison Arduino-compatible boards,
     * the port "IO13" has an onboard LED that turns on when the GPIO port is HIGH, and off
     * otherwise, so it is easy to debug that GPIO port' state without any extra hardware.
     */
    private static final String PREFERRED_GPIO_PORT = "IO13";

    private static final int NUMBER_OF_BLINKS = 30;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 50;

    public GPIOTask() {
        mService = new PeripheralManagerService();
    }

    @Override
    public void run() {
        Log.i(TAG, "Executing task GPIOTask");
        try {
            List<String> gpios = listGpios();
            if (gpios == null || gpios.isEmpty()) {
                return;
            }

            String portName;
            // if the preferred port exists, use it, otherwise use the first port identified.
            int preferred = gpios.indexOf(PREFERRED_GPIO_PORT);
            if (preferred >= 0) {
                portName = gpios.get(preferred);
            } else {
                portName = gpios.get(0);
            }

            // GPIO ports are single-user resources, so make sure you close them when you're done.
            // The "try with resources" syntax below is a good way of guaranteeing it.
            try (Gpio gpio = mService.openGpio(portName)) {
                gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                blink(gpio, NUMBER_OF_BLINKS, INTERVAL_BETWEEN_BLINKS_MS);
            }
            // Notice that the gpio resource will be closed anyway, either if the
            // try(){} block finishes successfully or if an exception is thrown.
            // If you do not use a try(resource){} block, make sure you close the gpio port
            // in a finally clause, otherwise an ErrnoException will be thrown when
            // openGpio is called again on this port.

        } catch (RemoteException | ErrnoException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private List<String> listGpios() throws RemoteException {
        // List available GPIOs:
        List<String> gpios = mService.getGpioList();
        if (gpios == null || gpios.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available GPIO ports: " + gpios);
        }
        return gpios;
    }

    private void blink(Gpio gpio, int numberOfBlinks, int intervalBetweenBlinksMs)
            throws RemoteException, ErrnoException {
        // Alternate GPIO output in a loop. Connect an LED to the port to see it blinking.
        for (int i=0; i < numberOfBlinks; i++) {

            // Turn on when i is even, off otherwise
            gpio.setValue( i % 2 == 0 );

            // Sleep for a while
            try {
                TimeUnit.MILLISECONDS.sleep(intervalBetweenBlinksMs);
            } catch (InterruptedException e) {
                // Ignore sleep interruption.
            }
        }

        // Turn off at the end
        gpio.setValue(false);
    }
}
