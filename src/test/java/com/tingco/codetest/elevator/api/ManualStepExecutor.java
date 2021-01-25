package com.tingco.codetest.elevator.api;

import java.util.concurrent.Semaphore;

public class ManualStepExecutor implements ElevatorStepExecutor {

    private final Semaphore semaphore = new Semaphore(0, true);

    @Override
    public void execute() {
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
