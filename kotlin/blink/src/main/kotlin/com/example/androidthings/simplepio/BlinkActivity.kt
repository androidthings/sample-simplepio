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
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import android.os.Bundle
import android.os.Handler
import android.util.Log

/**
 * Sample usage of the Gpio API that blinks an LED at a fixed interval defined in
 * [.INTERVAL_BETWEEN_BLINKS_MS].
 *
 * Some boards, like Intel Edison, have onboard LEDs linked to specific GPIO pins.
 * The preferred GPIO pin to use on each board is in the [BoardDefaults] class.
 */
class BlinkActivity : Activity() {
    private val handler = Handler()
    private lateinit var ledGpio: Gpio
    private var ledState = false

    private val blinkRunnable = object : Runnable {
        override fun run() {
            // Toggle the GPIO state
            ledState = !ledState
            ledGpio.value = ledState
            Log.d(TAG, "State set to ${ledState}")

            // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
            handler.postDelayed(this, INTERVAL_BETWEEN_BLINKS_MS)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting BlinkActivity")

        ledGpio = PeripheralManager.getInstance().openGpio(BoardDefaults.gpioForLED)
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        Log.i(TAG, "Start blinking LED GPIO pin")
        // Post a Runnable that continuously switch the state of the GPIO, blinking the
        // corresponding LED
        handler.post(blinkRunnable)

    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove pending blink Runnable from the handler.
        handler.removeCallbacks(blinkRunnable)
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin")
        ledGpio.close()
    }

    companion object {
        private val TAG = BlinkActivity::class.simpleName
        private const val INTERVAL_BETWEEN_BLINKS_MS = 1000L
    }
}