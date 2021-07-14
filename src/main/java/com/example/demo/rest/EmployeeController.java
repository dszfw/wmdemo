package com.example.demo.rest;

import com.example.demo.dto.ErrorResponseDto;
import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.EmployeeStateServiceImpl;
import com.example.demo.statemachine.EmployeeEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeStateServiceImpl stateMachineService;
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeStateServiceImpl stateMachineService,
        EmployeeService employeeService) {
        this.stateMachineService = stateMachineService;
        this.employeeService = employeeService;
    }

    @Operation(summary = "Add an employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee successfully added",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = Employee.class))})
    })
    @PostMapping
    public ResponseEntity<Employee> add(@RequestBody Employee employee) {
        Employee persistedEmployee = employeeService.add(employee);
        stateMachineService.changeState(persistedEmployee.getId(), EmployeeEvent.ADD);
        return ResponseEntity.ok(persistedEmployee);
    }

    @Operation(summary = "Send an event to change the state of given employee",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = EmployeeEvent.class)))
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "State successfully changed",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = Employee.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input data provided",
            content = {@Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class))})
    })
    @PostMapping("/{id}/event")
    public ResponseEntity<?> sendEvent(
        @RequestBody EmployeeEvent event,
        @PathVariable("id") Long employeeId
    ) {
        if (!employeeService.exists(employeeId)) {
            return badRequest("invalid id provided");
        }

        boolean stateChanged = stateMachineService.changeState(employeeId, event);
        if (stateChanged) {
            return badRequest(String.format("event %s is not possible for %s state", event,
                stateMachineService.getCurrentState(employeeId)));
        }

        return ResponseEntity.ok(employeeService.findById(employeeId));
    }

    private ResponseEntity<ErrorResponseDto> badRequest(String errorMessage) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto(errorMessage));
    }

}
