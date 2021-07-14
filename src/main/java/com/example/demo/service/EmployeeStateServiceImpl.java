package com.example.demo.service;

import static com.example.demo.statemachine.Constants.EMPLOYEE_ID_HEADER;

import com.example.demo.statemachine.EmployeeEvent;
import com.example.demo.statemachine.EmployeeState;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EmployeeStateServiceImpl implements EmployeeStateService {

    private final StateMachineFactory<EmployeeState, EmployeeEvent> stateMachineFactory;
    private final StateMachinePersister<EmployeeState, EmployeeEvent, String> stateMachinePersister;

    public EmployeeStateServiceImpl(
        StateMachineFactory<EmployeeState, EmployeeEvent> stateMachineFactory,
        StateMachinePersister<EmployeeState, EmployeeEvent, String> stateMachinePersister) {
        this.stateMachineFactory = stateMachineFactory;
        this.stateMachinePersister = stateMachinePersister;
    }

    @Override
    public boolean changeState(Long employeeId,
        EmployeeEvent event) {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine =
            restoreStateMachine(employeeId.toString());

        StateMachineEventResult<EmployeeState, EmployeeEvent> result =
            sendMessage(stateMachine, MessageBuilder.withPayload(event)
                .setHeader(EMPLOYEE_ID_HEADER, employeeId)
                .build());

        persistStateMachine(stateMachine, employeeId.toString());
        return result.getResultType() == ResultType.ACCEPTED;
    }

    @Override
    public EmployeeState getCurrentState(Long employeeId) {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine =
            restoreStateMachine(employeeId.toString());
        return stateMachine.getState().getId();
    }

    private StateMachineEventResult<EmployeeState, EmployeeEvent> sendMessage(
        StateMachine<EmployeeState, EmployeeEvent> stateMachine,
        Message<EmployeeEvent> message
    ) {
        return stateMachine.sendEvent(Mono.just(message)).blockLast();
    }

    private void persistStateMachine(
        StateMachine<EmployeeState, EmployeeEvent> stateMachine,
        String id
    ) {
        try {
            stateMachinePersister.persist(stateMachine, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StateMachine<EmployeeState, EmployeeEvent> restoreStateMachine(String id) {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine =
            stateMachineFactory.getStateMachine(id);
        try {
            stateMachinePersister.restore(stateMachine, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stateMachine;
    }

}
