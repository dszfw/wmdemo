package com.example.demo.statemachine.action;

import com.example.demo.statemachine.EmployeeEvent;
import com.example.demo.statemachine.EmployeeState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
public class AddEmployeeAction implements Action<EmployeeState, EmployeeEvent> {

    @Override
    public void execute(StateContext<EmployeeState, EmployeeEvent> context) {
        // do nothing
    }

}
