package com.tingco.codetest.elevator.resources.model;

import com.tingco.codetest.elevator.api.Elevator;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ElevatorModel {

    enum Direction {
        UP, DOWN, NONE
    }

    private final int id;
    private final Direction direction;
    private final int addressedFloor;
    private final boolean isBusy;
    private final int currentFloor;

    public static ElevatorModel of(Elevator elevator){
        return ElevatorModel.builder()
                .id(elevator.getId())
                .direction(Direction.valueOf(elevator.getDirection().name()))
                .addressedFloor(elevator.getAddressedFloor())
                .isBusy(elevator.isBusy())
                .currentFloor(elevator.currentFloor())
                .build();
    }
}
