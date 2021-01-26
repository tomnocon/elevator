package com.tingco.codetest.elevator.api.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

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
