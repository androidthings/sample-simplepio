package com.google.samples.simplepio;

import android.os.Handler;
import android.os.HandlerThread;
import android.service.headless.HomeService;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;

import com.google.samples.simplepio.task.GPIOTask;
import com.google.samples.simplepio.task.LEDTask;

/**
 * Simple {@link HomeService} that executes {@link Runnable} in a separate thread.
 *
 * When certain input events (Intel Edison onboard PWR and RM buttons) are received,
 * this class cycles through a list of tasks ({@link Runnable}) and schedules the selected task
 * to execute in a separate thread.
 */
public class SimplePIOService extends HomeService {
    public static final String TAG = "SimplePIOService";

    private static final int BUTTON_EDISON_RM_KEYCODE = 148;
    private static final int BUTTON_EDISON_PWR_KEYCODE = 116;

    private HandlerThread mRunnerThread;
    private Handler mRunnerThreadHandler;
    private int mCurrentTask;
    private Runnable[] mTasks;

    @Override
    public void onCreate() {
        Log.d(TAG, "Headless service created");
        mRunnerThread = new HandlerThread("runnerThread");
        mRunnerThread.start();
        mRunnerThreadHandler = new Handler(mRunnerThread.getLooper());

        mCurrentTask = 0;
        mTasks = new Runnable[] {
                new GPIOTask(),
                new LEDTask()
        };

        Log.i(TAG, mTasks.length + " tasks available. Current task is " +
                getTaskName(mTasks[mCurrentTask]));
        Log.i(TAG, "Press the RM onboard button to select another task.");
        Log.i(TAG, "Press the PWR onboard button to execute the current task.");
    }

    private String getTaskName(Runnable r) {
        return r.getClass().getSimpleName();
    }

    @Override
    public void onInputEvent(InputEvent event) {
        if (((KeyEvent) event).getAction() == KeyEvent.ACTION_DOWN) {
            switch (((KeyEvent) event).getScanCode()) {
                case BUTTON_EDISON_RM_KEYCODE:
                    mCurrentTask = (mCurrentTask + 1) % mTasks.length;
                    Log.d(TAG, "Button RM pressed. Changed current task to " + mCurrentTask +
                            " (" + getTaskName(mTasks[mCurrentTask]) + "). Now press " +
                            "PWR to execute it.");
                    break;
                case BUTTON_EDISON_PWR_KEYCODE:
                    Log.d(TAG, "Button PWR pressed. Executing task " + mCurrentTask +
                            " (" + getTaskName(mTasks[mCurrentTask]) + ")");
                    executeCurrentTask();
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mRunnerThread != null) {
            mRunnerThread.quit();
        }
    }

    private void executeCurrentTask() {
        Log.d(TAG, "Executing task " + mCurrentTask +
                " (" + getTaskName(mTasks[mCurrentTask]) + ")");
        Runnable r = mTasks[mCurrentTask];

        // It is a good practice to execute I/O code in a separate thread
        mRunnerThreadHandler.post(r);
    }

}
