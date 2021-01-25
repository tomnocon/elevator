package com.tingco.codetest.elevator.api;

import com.tingco.codetest.elevator.api.events.ElevatorEvent;
import com.tingco.codetest.elevator.api.events.ElevatorMoved;
import com.tingco.codetest.elevator.api.events.ElevatorReleased;
import com.tingco.codetest.elevator.api.events.ElevatorRequested;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ElevatorControllerTests {

    @Test
    void shouldFollowFloorDirection() {
        // Arrange
        val desiredFloor = 2;
        ElevatorController elevatorController = null;

        // Act
        val elevator = elevatorController.requestElevator(desiredFloor);

        // Assert
        assertEquals(Elevator.Direction.UP, elevator.getDirection());
        assertEquals(desiredFloor, elevator.getAddressedFloor());
    }

    @Test
    void shouldQueueElevatorRequests() {
        // Arrange
        val events = new LinkedList<ElevatorEvent>();
        ElevatorEventPublisher publisher = events::add;
        ManualStepExecutor manualExecutor = null;
        ElevatorController elevatorController = null; // one elevator available

        // Act
        val firstRequest = elevatorController.requestElevator(2);
        val secondRequest = elevatorController.requestElevator(4);
        manualExecutor.allSteps();

        // Asset
        val elevatorMoves = ofEventTypes(events, ElevatorMoved.class);
        val elevatorRequests = ofEventTypes(events, ElevatorRequested.class);
        val elevatorReleases = ofEventTypes(events, ElevatorReleased.class);
        assertSame(firstRequest, secondRequest);
        assertEquals(firstRequest.currentFloor(), 4);
        assertEquals(4, elevatorMoves.size());
        assertEquals(1, elevatorMoves.get(0).getFloor());
        assertEquals(2, elevatorMoves.get(1).getFloor());
        assertEquals(3, elevatorMoves.get(2).getFloor());
        assertEquals(4, elevatorMoves.get(3).getFloor());
        assertEquals(2, elevatorRequests.size());
        assertEquals(1, elevatorReleases.size());
    }

    @Test
    void shouldHandleMultipleElevators() {
        // Arrange
        val events = new LinkedList<ElevatorEvent>();
        ElevatorEventPublisher publisher = events::add;
        ManualStepExecutor manualExecutor = null;
        ElevatorController elevatorController = null; // two elevators available

        // Act
        val firstElevator = elevatorController.requestElevator(2);
        val secondElevator = elevatorController.requestElevator(1);
        manualExecutor.allSteps();

        // Asset
        val elevatorMoves = ofEventTypes(events, ElevatorMoved.class);
        val elevatorRequests = ofEventTypes(events, ElevatorRequested.class);
        val elevatorReleases = ofEventTypes(events, ElevatorReleased.class);
        assertNotSame(firstElevator, secondElevator);
        assertEquals(2, ofElevatorId(elevatorMoves, firstElevator.getId()).size());
        assertEquals(1, ofElevatorId(elevatorMoves, secondElevator.getId()).size());
        assertEquals(2, elevatorRequests.size());
        assertEquals(2, elevatorReleases.size());
    }



    static <T> List<T> ofEventTypes(List<?> events, Class<T> type) {
        return events
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    static <T extends ElevatorEvent> List<T> ofElevatorId(List<T> events, int elevatorId) {
        return events.stream()
                .filter(event -> event.getElevatorId() == elevatorId)
                .collect(Collectors.toList());
    }

}
