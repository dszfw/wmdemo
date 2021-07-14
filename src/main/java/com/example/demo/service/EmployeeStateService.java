package com.example.demo.service;

import com.example.demo.statemachine.EmployeeEvent;
import com.example.demo.statemachine.EmployeeState;

public interface EmployeeStateService {

    boolean changeState(Long employeeId, EmployeeEvent event);

    EmployeeState getCurrentState(Long employeeId);

}
