/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.simplepio;

import android.app.Activity;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Sample usage of the Gpio API that logs when a button is pressed.
 *
 */
public class ButtonActivity extends Activity {
    private static final String TAG = ButtonActivity.class.getSimpleName();

    private Gpio mButtonGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");



        try {

            String pinName = BoardDefaults.getGPIOForButton();
            mButtonGpio = PeripheralManager.getInstance().openGpio(pinName);
            // Initialize the pin as an input
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            // High voltage is considered active
            mButtonGpio.setActiveType(Gpio.ACTIVE_LOW);

            // Register for all state changes
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_RISING);


            mButtonGpio.registerGpioCallback(new GpioCallback() {
                int counter=0;
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    // Return true to continue listening to events
                    counter++;
                    Toast.makeText(ButtonActivity.this, "Button clicked counter :"+counter, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButtonGpio != null) {
            // Close the Gpio pin
            Log.i(TAG, "Closing Button GPIO pin");
            try {
                mButtonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            } finally {
                mButtonGpio = null;
            }
        }
    }
}
