package com.example.demo.statemachine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

public class EmployeeStateMachinePersist implements
    StateMachinePersist<EmployeeState, EmployeeEvent, String> {

    private final Map<String, StateMachineContext<EmployeeState, EmployeeEvent>> contexts = new ConcurrentHashMap<>();

    @Override
    public void write(StateMachineContext<EmployeeState, EmployeeEvent> context,
        String contextObj) {
        contexts.put(contextObj, context);
    }

    @Override
    public StateMachineContext<EmployeeState, EmployeeEvent> read(String contextObj) {
        return contexts.get(contextObj);
    }

}
