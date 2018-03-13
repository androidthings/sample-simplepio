/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.androidthings.simplepio

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager

/**
 * Sample usage of the Gpio API that logs when a button is pressed.
 *
 */
class ButtonActivity : Activity() {
    private lateinit var buttonGpio: Gpio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting ButtonActivity")

        buttonGpio = PeripheralManager.getInstance().openGpio(BoardDefaults.gpioForButton)
        buttonGpio.setDirection(Gpio.DIRECTION_IN)
        buttonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING)
        buttonGpio.registerGpioCallback {
            Log.i(TAG, "GPIO changed, button pressed")
            // Return true to continue listening to events
            true
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Close the Gpio pin
        Log.i(TAG, "Closing Button GPIO pin")
        buttonGpio.close()
    }

    companion object {
        private val TAG = ButtonActivity::class.simpleName
    }
}