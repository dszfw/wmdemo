package com.example.demo.service;

import com.example.demo.statemachine.EmployeeEvent;
import com.example.demo.statemachine.EmployeeState;
import org.springframework.statemachine.StateMachineEventResult;

public interface EmployeeStateMachineService {

    StateMachineEventResult<EmployeeState, EmployeeEvent> changeState(Long employeeId, EmployeeEvent event);

    EmployeeState getCurrentState(Long employeeId);

}
