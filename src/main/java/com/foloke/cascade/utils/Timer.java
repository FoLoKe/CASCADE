package com.foloke.cascade.utils;

public abstract class Timer {
    double time;
    double delay;

    public Timer(long delay) {
        this.time = System.nanoTime();
        this.delay = delay;
    }

    public void tick(double timestamp) {
        if(timestamp - time > delay) {
            execute();
            time = timestamp;
        }
    }

    public abstract void execute();
}
