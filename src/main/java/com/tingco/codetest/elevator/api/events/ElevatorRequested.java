package com.tingco.codetest.elevator.api.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ElevatorRequested implements ElevatorEvent {
    private final int elevatorId;

    @Override
    public Integer getElevatorId() {
        return elevatorId;
    }
}
