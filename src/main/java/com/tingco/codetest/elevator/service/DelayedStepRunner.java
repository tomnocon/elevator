package com.tingco.codetest.elevator.service;

import com.tingco.codetest.elevator.api.ElevatorStepRunner;

public class DelayedStepRunner implements ElevatorStepRunner {

    private final int delayInMillis;

    public DelayedStepRunner(int delayInMillis) {
        this.delayInMillis = delayInMillis;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(delayInMillis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
