package com.tingco.codetest.elevator.service;

import com.google.common.eventbus.EventBus;
import com.tingco.codetest.elevator.api.ElevatorEventPublisher;
import com.tingco.codetest.elevator.api.events.ElevatorEvent;

public class EventBusPublisher implements ElevatorEventPublisher {
    private final EventBus eventBus;

    public EventBusPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void publish(ElevatorEvent elevatorEvent) {
        eventBus.post(elevatorEvent);
    }
}
