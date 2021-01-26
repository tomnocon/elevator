package com.tingco.codetest.elevator.service;

import com.tingco.codetest.elevator.api.Elevator;
import com.tingco.codetest.elevator.api.ElevatorController;
import com.tingco.codetest.elevator.api.ElevatorEventPublisher;
import com.tingco.codetest.elevator.api.events.ElevatorEvent;
import com.tingco.codetest.elevator.api.events.ElevatorMoved;
import com.tingco.codetest.elevator.api.events.ElevatorReleased;
import com.tingco.codetest.elevator.api.events.ElevatorRequested;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadedElevatorControllerTests {

    @Test
    void shouldQueueElevatorRequests() {
        // Arrange
        val events = new LinkedList<ElevatorEvent>();
        ElevatorEventPublisher publisher = events::add;
        val manualRunner = new ManualStepRunner();
        ElevatorController elevatorController = new ThreadedElevatorController(1, Executors.newSingleThreadExecutor(), manualRunner, publisher);

        // Act
        val firstRequest = elevatorController.requestElevator(2);
        val secondRequest = elevatorController.requestElevator(4);
        manualRunner.allSteps();
        waitForElevators(firstRequest, secondRequest);

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
    void shouldHandleMultipleElevators() throws InterruptedException {
        // Arrange
        val events = new LinkedList<ElevatorEvent>();
        ElevatorEventPublisher publisher = events::add;
        val manualRunner = new ManualStepRunner();
        ElevatorController elevatorController = new ThreadedElevatorController(2, Executors.newSingleThreadExecutor(), manualRunner, publisher);

        // Act
        val firstElevator = elevatorController.requestElevator(2);
        val secondElevator = elevatorController.requestElevator(1);
        manualRunner.allSteps();
        waitForElevators(firstElevator, secondElevator);

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

    static void waitForElevators(Elevator... elevators) {
        for (val elevator : elevators) {
            while (elevator.isBusy()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

}
