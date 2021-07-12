package com.example.demo.statemachine.action;

import static com.example.demo.statemachine.Constants.EMPLOYEE_ID_HEADER;

import com.example.demo.service.EmployeeService;
import com.example.demo.statemachine.EmployeeEvent;
import com.example.demo.statemachine.EmployeeState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
public class ApproveEmployeeAction implements Action<EmployeeState, EmployeeEvent> {

    private final EmployeeService employeeService;

    public ApproveEmployeeAction(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void execute(StateContext<EmployeeState, EmployeeEvent> context) {
        Long employeeId = context.getMessage().getHeaders().get(EMPLOYEE_ID_HEADER, Long.class);
        employeeService.approve(employeeId);
    }

}
