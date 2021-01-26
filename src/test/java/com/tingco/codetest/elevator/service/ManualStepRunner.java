package com.tingco.codetest.elevator.service;

import com.tingco.codetest.elevator.api.ElevatorStepRunner;

import java.util.concurrent.Semaphore;

public class ManualStepRunner implements ElevatorStepRunner {

    private final Semaphore semaphore = new Semaphore(0);

    @Override
    public void run() {
        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void nextStep() {
        semaphore.release();
    }

    public void allSteps(){
        semaphore.release(Integer.MAX_VALUE);
    }
}
