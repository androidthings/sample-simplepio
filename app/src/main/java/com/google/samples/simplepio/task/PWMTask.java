package com.google.samples.simplepio.task;

import android.os.RemoteException;
import android.pio.PeripheralManagerService;
import android.system.ErrnoException;
import android.util.Log;

import com.google.samples.simplepio.SimplePIOService;
import com.google.samples.simplepio.peripherals.Servo;

import java.util.concurrent.TimeUnit;

/**
 * Simple PWM task to control a servo. Update the constants {@link #MIN_PULSE_DURATION}, {@link #MAX_PULSE_DURATION}
 * and {@link #MAX_DEG} to match the parameters of your servo.
 */

public class PWMTask extends Task {
    private static final String TAG = SimplePIOService.TAG;

    private static final String SERVO_PWM_PORT = "IO6";

    /**
     * Minimum PWM duty cycle, in milliseconds. Most servos have minimum duty cycle around 1 ms.
     * To find the correct value for the servo you are using, you can either look at the
     * servo data sheet or experiment stretching the ranges and observing the servo behavior.
     */
    private static final double MIN_PULSE_DURATION = 0.9;

    /**
     * Maximum PWM duty cycle, in milliseconds. Most servos have maximum duty cycle around 2 ms.
     * To find the correct value for the servo you are using, you can either look at the
     * servo data sheet or experiment stretching the ranges and observing the servo behavior.
     */
    private static final double MAX_PULSE_DURATION = 2.09;

    private static final int MAX_DEG = 180;
    private static final int DEGREES_PER_EXECUTION = 10;
    private static final int TIME_BETWEEN_ANGLE_CHANGES_MS = 200; // ms

    private Servo mServo;
    private double mCurrentAngle = 0;

    @Override
    public void run() {
        Log.i(TAG, "Executing task PWMTask. Current angle=" + mCurrentAngle);

        try {
            maybeOpenServo();

            // moves one degree per TIME_BETWEEN_ANGLE_CHANGES_MS
            // Note: most servos don't have enough precision for changes as small as 1 degree.
            // the expected effect of this loop in most commercially available affordable servos is
            // that the servo will only move once every ~10 degrees. This loop might seem useless,
            // but it helps understanding the limits and behavior of the servo.
            for (int i = 0; i < DEGREES_PER_EXECUTION; i++) {
                mServo.set(mCurrentAngle);
                mCurrentAngle = (mCurrentAngle + 1) % MAX_DEG;
                try {
                    TimeUnit.MILLISECONDS.sleep(TIME_BETWEEN_ANGLE_CHANGES_MS);
                } catch (InterruptedException e) {
                    // ignore sleep interruption
                }
            }
        } catch (RemoteException | ErrnoException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private void maybeOpenServo() throws RemoteException, ErrnoException {
        if (mServo == null) {
            PeripheralManagerService service = new PeripheralManagerService();
            mServo = new Servo(service);
            mServo.open(SERVO_PWM_PORT);
            mServo.setAngleRange(0, MAX_DEG);
            mServo.setPulseDurationRange(MIN_PULSE_DURATION, MAX_PULSE_DURATION);
        }
    }

    @Override
    public void releaseResources() {
        if (mServo != null) {
            mServo.close();
            mServo = null;
        }
    }
}
