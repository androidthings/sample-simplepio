package com.google.samples.simplepio;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.hardware.pio.Gpio;
import android.hardware.pio.PeripheralManagerService;
import android.system.ErrnoException;
import android.util.Log;

import java.util.List;

/**
 * BlinkActivity is an example that use the Gpio API to blink an LED at a fixed interval.
 */
public class BlinkActivity extends Activity {
    private static final String TAG = "BlinkActivity";
    /**
     * GPIO pin the LED is connected on.
     * For example, on Intel Edison Arduino breakout, pin "IO13" is connected to an onboard LED
     * that turns on when the GPIO pin is HIGH, and off when low.
     */
    private static final String LED_GPIO_PIN = "26";
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;

    private PeripheralManagerService mService;
    private Handler mHandler = new Handler();
    private Runnable mBlinkRunnable = this::blinkLed;
    private Gpio mLedGpio;
    private boolean mLedState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting BlinkActivity");

        mService = new PeripheralManagerService();
        try {
            // List available GPIOs:
            List<String> gpios = mService.getGpioList();
            if (gpios == null || gpios.isEmpty()) {
                Log.e(TAG, "No GPIO pins available on this device.");
                return;
            }
            Log.i(TAG, "List of available GPIO pins: " + gpios);
            if (!gpios.contains(LED_GPIO_PIN)) {
                Log.e(TAG, "LED GPIO pin not found: " + LED_GPIO_PIN);
                return;
            }
            // Open the LED GPIO pin.
            Log.i(TAG, "Opening LED GPIO pin: " + LED_GPIO_PIN);
            mLedGpio = mService.openGpio(LED_GPIO_PIN);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedState = false;
            Log.i(TAG, "Start blinking LED GPIO pin");
            // Post a Runnable that conntinously blink the LED GPIO.
            mHandler.post(mBlinkRunnable);
        } catch (RemoteException | ErrnoException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy(){
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        mLedGpio.close();
        mLedGpio = null;
    }

    private void blinkLed() {
        // Exit Runnable if the LED GPIO is already closed.
        if (mLedGpio == null) {
            return;
        }
        try {
            // Toggle the GPIO.
            mLedState = !mLedState;
            mLedGpio.setValue(mLedState);
            // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds.
            mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
        } catch (RemoteException | ErrnoException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }
}
