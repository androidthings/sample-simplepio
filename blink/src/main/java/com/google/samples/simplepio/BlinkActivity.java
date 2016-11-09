package com.google.samples.simplepio;

import android.app.Activity;
import android.hardware.pio.Gpio;
import android.hardware.pio.PeripheralManagerService;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

/**
 * Sample usage of the Gpio API that blinks an LED at a fixed interval defined in
 * {@link #INTERVAL_BETWEEN_BLINKS_MS}.
 *
 * Some boards, like Intel Edison, have onboard LEDs linked to specific GPIO pins.
 * The preferred GPIO pin to use on each board is in the {@link BoardDefaults} class.
 *
 */
public class BlinkActivity extends Activity {
    private static final String TAG = BlinkActivity.class.getSimpleName();
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;

    private Handler mHandler = new Handler();
    private Runnable mBlinkRunnable = this::blinkLed;
    private Gpio mLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting BlinkActivity");

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            String pinName = BoardDefaults.getGPIOForLED();
            mLedGpio = service.openGpio(pinName);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            Log.i(TAG, "Start blinking LED GPIO pin");
            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            mLedGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio = null;
        }
    }

    private void blinkLed() {
        // Exit Runnable if the GPIO is already closed
        if (mLedGpio == null) {
            return;
        }
        try {
            // Toggle the GPIO state
            mLedGpio.setValue(!mLedGpio.getValue());
            Log.d(TAG, "State set to " + mLedGpio.getValue());

            // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
            mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }
}
