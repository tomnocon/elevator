package com.tingco.codetest.elevator.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.tingco.codetest.elevator.api.ElevatorController;
import com.tingco.codetest.elevator.api.ElevatorEventPublisher;
import com.tingco.codetest.elevator.api.ElevatorStepRunner;
import com.tingco.codetest.elevator.service.DelayedStepRunner;
import com.tingco.codetest.elevator.service.EventBusPublisher;
import com.tingco.codetest.elevator.service.ThreadedElevatorController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ElevatorConfiguration {
    @Value("${com.tingco.elevator.numberofelevators}")
    private int numberOfElevators;

    @Value("${com.tingco.elevator.elevatorStepDurationInMillis}")
    private int elevatorStepDurationInMillis;


    /**
     * Create a default thread pool for your convenience.
     *
     * @return Executor thread pool
     */
    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(numberOfElevators);
    }

    /**
     * Create an event bus for your convenience.
     *
     * @return EventBus for async task execution
     */
    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(Executors.newCachedThreadPool());
    }

    @Bean
    public ElevatorEventPublisher eventPublisher(EventBus eventBus){
        return new EventBusPublisher(eventBus);
    }

    @Bean
    public ElevatorStepRunner elevatorStepRunner(){
        return new DelayedStepRunner(elevatorStepDurationInMillis);
    }

    @Bean
    public ElevatorController elevatorController(Executor taskExecutor, ElevatorEventPublisher eventPublisher, ElevatorStepRunner elevatorStepRunner){
        return new ThreadedElevatorController(numberOfElevators, taskExecutor, elevatorStepRunner, eventPublisher);
    }
}
