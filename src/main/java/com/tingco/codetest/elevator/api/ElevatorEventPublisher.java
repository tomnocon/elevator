package com.tingco.codetest.elevator.api;

import com.tingco.codetest.elevator.api.events.ElevatorEvent;

public interface ElevatorEventPublisher {
    void publish(ElevatorEvent elevatorEvent);
}
