package com.tingco.codetest.elevator.resources;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tingco.codetest.elevator.api.Elevator;
import com.tingco.codetest.elevator.api.ElevatorController;
import com.tingco.codetest.elevator.api.events.ElevatorEvent;
import com.tingco.codetest.elevator.resources.model.ElevatorModel;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Rest Resource.
 *
 * @author Sven Wesley
 *
 */
@RestController
@RequestMapping("/rest/v1")
public final class ElevatorControllerEndPoints {

    private final ElevatorController elevatorController;
    private final EventBus eventBus;

    public ElevatorControllerEndPoints(ElevatorController elevatorController, EventBus eventBus) {
        this.elevatorController = elevatorController;
        this.eventBus = eventBus;
    }

    /**
     * Ping service to test if we are alive.
     *
     * @return String pong
     */
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping() {
        return "pong";
    }

    @GetMapping(value = "/elevators")
    public ResponseEntity<List<ElevatorModel>> getElevators() {
        val elevators =  this.elevatorController
                .getElevators()
                .stream()
                .map(ElevatorModel::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok(elevators);
    }

    @GetMapping(value = "/elevators/{id}")
    public ResponseEntity<ElevatorModel> getElevator(@PathVariable Integer id) {
        return this.elevatorController
                .getElevators()
                .stream()
                .filter(elevator -> elevator.getId() == id)
                .map(ElevatorModel::of)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping(value = "/elevators/floors/{floor}")
    public ResponseEntity<ElevatorModel> requestElevator(@PathVariable Integer floor) {
        val elevator = elevatorController.requestElevator(floor);
        return ResponseEntity.ok(ElevatorModel.of(elevator));
    }

    @GetMapping(value = "/live-elevators/{id}")
    public SseEmitter liveElevator(@PathVariable Integer id) {
        val emitter = new SseEmitter();
        val listener = new ElevatorEventListener(emitter, eventBus, elevatorId -> elevatorId.equals(id));
        listener.register();
        return emitter;
    }

    @GetMapping(value = "/live-elevators")
    public SseEmitter liveElevators() {
        val emitter = new SseEmitter();
        val listener = new ElevatorEventListener(emitter, eventBus, elevatorId -> true);
        listener.register();
        return emitter;
    }

    static class ElevatorEventListener {
        private final Predicate<Integer> elevatorFilter;
        private final SseEmitter emitter;
        private final EventBus eventBus;

        public ElevatorEventListener(SseEmitter emitter, EventBus eventBus,Predicate<Integer> elevatorFilter) {
            this.elevatorFilter = elevatorFilter;
            this.emitter = emitter;
            this.eventBus = eventBus;
        }

        @Subscribe
        public void onEvent(ElevatorEvent event) {
            try {
                if(elevatorFilter.test(event.getElevatorId())){
                    emitter.send(event);
                }
            } catch(Exception e) {
                emitter.complete();
                unregister();
            }
        }

        public void unregister(){
            try {
                eventBus.unregister(this);
            }catch (IllegalArgumentException ex){
                // Already unregistered
            }
        }

        public void register(){
            eventBus.register(this);
        }
    }
}
