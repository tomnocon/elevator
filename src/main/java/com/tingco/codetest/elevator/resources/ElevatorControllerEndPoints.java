package com.tingco.codetest.elevator.resources;

import com.tingco.codetest.elevator.api.ElevatorController;
import com.tingco.codetest.elevator.resources.model.ElevatorModel;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    public ElevatorControllerEndPoints(ElevatorController elevatorController) {
        this.elevatorController = elevatorController;
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

    @RequestMapping(value = "/elevators", method = RequestMethod.GET)
    public ResponseEntity<List<ElevatorModel>> getElevators() {
        val elevators =  this.elevatorController
                .getElevators()
                .stream()
                .map(ElevatorModel::of)
                .collect(Collectors.toList());

        return ResponseEntity.ok(elevators);
    }

    @RequestMapping(value = "/elevators/{id}", method = RequestMethod.GET)
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

    @RequestMapping(value = "/elevators/floors/{floor}", method = RequestMethod.GET)
    public ResponseEntity<ElevatorModel> requestElevator(@PathVariable Integer floor) {
        val elevator = elevatorController.requestElevator(floor);
        return ResponseEntity.ok(ElevatorModel.of(elevator));
    }
}
