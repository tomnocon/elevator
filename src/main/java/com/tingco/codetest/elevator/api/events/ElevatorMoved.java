package com.tingco.codetest.elevator.api.events;

import lombok.Getter;

@Getter
public class ElevatorMoved extends ElevatorEvent {
    private int floor;
}
