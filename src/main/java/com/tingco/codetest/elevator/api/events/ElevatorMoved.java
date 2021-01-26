package com.tingco.codetest.elevator.api.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ElevatorMoved implements ElevatorEvent {
    private final int elevatorId;
    private final int floor;

    @Override
    public Integer getElevatorId() {
        return elevatorId;
    }
}
