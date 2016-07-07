package com.google.samples.simplepio.task;

import android.os.RemoteException;
import android.pio.Led;
import android.pio.PeripheralManagerService;
import android.system.ErrnoException;
import android.util.Log;

import com.google.samples.simplepio.SimplePIOService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Simple onboard LED handling.
 */

public class LEDTask extends Task {
    private static final String TAG = SimplePIOService.TAG;

    private PeripheralManagerService mService;

    private static final int NUMBER_OF_BLINKS = 10;
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 200;

    public LEDTask() {
        mService = new PeripheralManagerService();
    }

    @Override
    public void run() {
        Log.i(TAG, "Executing task LEDTask");
        try {
            List<String> leds = listLeds();
            if (leds == null || leds.isEmpty()) {
                return;
            }

            for (String ledName : leds) {
                // LEDs are single-user resources, so make sure you close them when you're done.
                // The "try with resources" syntax below is a good way of guaranteeing it.
                try (Led led = mService.openLed(ledName)) {
                    blink(led, NUMBER_OF_BLINKS, INTERVAL_BETWEEN_BLINKS_MS);
                }
                // Notice that the led resource will be closed anyway, either if the
                // try(){} block finishes successfully or if an exception is thrown.
                // If you do not use a try(resource){} block, make sure you close the led
                // in a finally clause, otherwise an ErrnoException will be thrown when
                // openLed is called again with the same LED name.
            }

        } catch (RemoteException | ErrnoException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private List<String> listLeds() throws RemoteException {
        // List available LEDs:
        List<String> leds = mService.getLedList();
        if (leds == null || leds.isEmpty()) {
            Log.i(TAG, "No onboard LED available on this device. Try connecting an led to a GPIO port.");
        } else {
            Log.i(TAG, "List of available LEDs: " + leds);
        }
        return leds;
    }

    private void blink(Led led, int numberOfBlinks, int intervalBetweenBlinksMs)
            throws RemoteException, ErrnoException {
        int maxBrightness = led.getMaxBrightness();

        // Alternate LED brightness from zero to max in a loop.
        for (int i=0; i < numberOfBlinks; i++) {
            // Turn to maxBrightness when i is even, zero brightness otherwise
            int brightness = i % 2 == 0 ? maxBrightness : 0;
            led.setBrightness(brightness);

            // Sleep for a while
            try {
                TimeUnit.MILLISECONDS.sleep(intervalBetweenBlinksMs);
            } catch (InterruptedException e) {
                // Ignore sleep interruption.
            }
        }
    }
}
