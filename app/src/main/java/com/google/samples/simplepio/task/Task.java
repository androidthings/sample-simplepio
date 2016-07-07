package com.google.samples.simplepio.task;

/**
 * Abstract task with support for lifecycle methods.
 */

public abstract class Task implements Runnable {

    public abstract void run();

    public void releaseResources() {
    }
}
