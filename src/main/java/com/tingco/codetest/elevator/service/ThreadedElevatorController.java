package com.tingco.codetest.elevator.service;

import com.tingco.codetest.elevator.api.Elevator;
import com.tingco.codetest.elevator.api.ElevatorController;
import com.tingco.codetest.elevator.api.ElevatorEventPublisher;
import com.tingco.codetest.elevator.api.ElevatorStepRunner;
import com.tingco.codetest.elevator.api.events.ElevatorMoved;
import com.tingco.codetest.elevator.api.events.ElevatorReleased;
import com.tingco.codetest.elevator.api.events.ElevatorRequested;
import lombok.val;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.function.Predicate.not;

public class ThreadedElevatorController implements ElevatorController {

    private final Random random = new Random();
    private final Executor executor;
    private final ElevatorStepRunner stepRunner;
    private final ElevatorEventPublisher eventPublisher;
    private final List<Elevator> elevators;

    public ThreadedElevatorController(Integer numberOfElevators, Executor executor, ElevatorStepRunner stepRunner, ElevatorEventPublisher eventPublisher) {
        this.executor = executor;
        this.stepRunner = stepRunner;
        this.eventPublisher = eventPublisher;
        this.elevators = new ArrayList<>(numberOfElevators);
        for (int i = 0; i < numberOfElevators; i++) {
            elevators.add(new ThreadedElevator(i));
        }
    }

    @Override
    public Elevator requestElevator(int toFloor) {
        val elevator = this.elevators
                .stream()
                .filter(not(Elevator::isBusy))
                .findAny()
                .orElse(elevators.get(random.nextInt(elevators.size())));
        elevator.moveElevator(toFloor);
        eventPublisher.publish(new ElevatorRequested(elevator.getId()));
        return elevator;
    }

    @Override
    public List<Elevator> getElevators() {
        return Collections.unmodifiableList(this.elevators);
    }

    @Override
    public void releaseElevator(Elevator elevator) {
        eventPublisher.publish(new ElevatorReleased(elevator.getId()));
    }

    class ThreadedElevator implements Elevator {

        private final Integer id;
        private volatile Direction currentDirection = Direction.NONE;
        private final AtomicInteger addressedFloor = new AtomicInteger(0);
        private final Queue<Integer> addressedFloors = new ConcurrentLinkedQueue<>();
        private final AtomicInteger currentFloor = new AtomicInteger(0);
        private final AtomicBoolean isBusy = new AtomicBoolean(false);

        public ThreadedElevator(Integer id) {
            this.id = id;
        }

        @Override
        public Direction getDirection() {
            return currentDirection;
        }

        @Override
        public int getAddressedFloor() {
            return addressedFloor.get();
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void moveElevator(int toFloor) {
            addressedFloors.add(toFloor);
            val expected = isBusy.compareAndExchange(false, true);
            if (!expected) {
                runMovingLoop();
            }
        }

        @Override
        public boolean isBusy() {
            return isBusy.get();
        }

        @Override
        public int currentFloor() {
            return currentFloor.get();
        }

        private void runMovingLoop() {
            executor.execute(() -> {
                while (!addressedFloors.isEmpty()) {
                    addressedFloor.set(addressedFloors.poll());
                    moveToFloor(addressedFloor.get());
                }
                currentDirection = Direction.NONE;
                isBusy.set(false);
                releaseElevator(this);
            });
        }

        private void moveToFloor(int floor) {
            while (currentFloor.get() != floor) {
                currentDirection = currentFloor.get() < floor ? Direction.UP : Direction.DOWN;
                stepRunner.run();
                if (currentDirection == Direction.DOWN) {
                    currentFloor.decrementAndGet();
                } else {
                    currentFloor.incrementAndGet();
                }
                eventPublisher.publish(new ElevatorMoved(id, currentFloor.get()));
            }
        }
    }
}
