package com.example.demo.statemachine;

import static com.example.demo.statemachine.Constants.EMPLOYEE_HEADER;
import static com.example.demo.statemachine.Constants.EMPLOYEE_ID_HEADER;
import static com.example.demo.statemachine.EmployeeEvent.ACTIVATE;
import static com.example.demo.statemachine.EmployeeEvent.ADD;
import static com.example.demo.statemachine.EmployeeEvent.APPROVE;
import static com.example.demo.statemachine.EmployeeEvent.CHECK;
import static com.example.demo.statemachine.EmployeeState.ACTIVE;
import static com.example.demo.statemachine.EmployeeState.ADDED;
import static com.example.demo.statemachine.EmployeeState.APPROVED;
import static com.example.demo.statemachine.EmployeeState.IN_CHECK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.statemachine.StateMachineEventResult.ResultType.DENIED;

import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import com.example.demo.statemachine.EmployeeEvent;
import com.example.demo.statemachine.EmployeeState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import reactor.core.publisher.Mono;

@SpringBootTest
public class EmployeeStateMachineTests {

    @Autowired
    private StateMachineFactory<EmployeeState, EmployeeEvent> stateMachineFactory;

    @MockBean
    private EmployeeService employeeService;

    @Test
    public void givenEmployee_WhenAddEventSent_ThenStateIsAdded() throws Exception {
        Employee employee = givenNewEmployee();
        Message<EmployeeEvent> addEvent = givenEmployeeEvent(employee, ADD);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = stateMachineFactory.getStateMachine();

        StateMachineTestPlan<EmployeeState, EmployeeEvent> testPlan =
            givenStateMachineTestPlan(stateMachine, addEvent, ADDED);

        testPlan.test();
    }

    @Test
    public void givenAddedEmployee_WhenCheckEventSent_ThenStateIsInCheck() throws Exception {
        Employee employee = givenEmployee(1L);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenAddedStateMachine(employee);
        Message<EmployeeEvent> checkEvent = givenEmployeeEvent(employee, CHECK);

        StateMachineTestPlan<EmployeeState, EmployeeEvent> testPlan =
            givenStateMachineTestPlan(stateMachine, checkEvent, IN_CHECK);

        testPlan.test();

        verify(employeeService).check(any());
    }

    @Test
    public void givenInCheckEmployee_WhenApproveEventSent_ThenStateIsApproved() throws Exception {
        Employee employee = givenEmployee(1L);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenInCheckStateMachine(
            employee);
        Message<EmployeeEvent> approveEvent = givenEmployeeEvent(employee, APPROVE);

        StateMachineTestPlan<EmployeeState, EmployeeEvent> testPlan =
            givenStateMachineTestPlan(stateMachine, approveEvent, APPROVED);

        testPlan.test();

        verify(employeeService).approve(any());
    }

    @Test
    public void givenApprovedEmployee_WhenActivateEventSent_ThenStateIsActive() throws Exception {
        Employee employee = givenEmployee(1L);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenApprovedStateMachine(
            employee);
        Message<EmployeeEvent> activateEvent = givenEmployeeEvent(employee, ACTIVATE);

        StateMachineTestPlan<EmployeeState, EmployeeEvent> testPlan =
            givenStateMachineTestPlan(stateMachine, activateEvent, ACTIVE);

        testPlan.test();

        verify(employeeService).activate(any());
    }

    @ParameterizedTest
    @EnumSource(value = EmployeeEvent.class, names = "ADD", mode = Mode.EXCLUDE)
    public void givenInitEmployee_WhenWrongEventSent_TransitionIsDenied(EmployeeEvent event) {
        Employee employee = givenNewEmployee();
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = stateMachineFactory.getStateMachine();

        StateMachineEventResult<EmployeeState, EmployeeEvent> result =
            sendEvent(employee, stateMachine, event);

        assertThat(result.getResultType(), is(DENIED));
    }

    @ParameterizedTest
    @EnumSource(value = EmployeeEvent.class, names = "CHECK", mode = Mode.EXCLUDE)
    public void givenAddedEmployee_WhenWrongEventSent_TransitionIsDenied(EmployeeEvent event) {
        Employee employee = givenEmployee(1L);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenAddedStateMachine(employee);

        StateMachineEventResult<EmployeeState, EmployeeEvent> result =
            sendEvent(employee, stateMachine, event);

        assertThat(result.getResultType(), is(DENIED));
    }

    @ParameterizedTest
    @EnumSource(value = EmployeeEvent.class, names = "APPROVE", mode = Mode.EXCLUDE)
    public void givenInCheckEmployee_WhenWrongEventSent_TransitionIsDenied(EmployeeEvent event) {
        Employee employee = givenEmployee(1L);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenInCheckStateMachine(employee);

        StateMachineEventResult<EmployeeState, EmployeeEvent> result =
            sendEvent(employee, stateMachine, event);

        assertThat(result.getResultType(), is(DENIED));
    }

    @ParameterizedTest
    @EnumSource(value = EmployeeEvent.class, names = "ACTIVATE", mode = Mode.EXCLUDE)
    public void givenApprovedEmployee_WhenWrongEventSent_TransitionIsDenied(EmployeeEvent event) {
        Employee employee = givenEmployee(1L);
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenApprovedStateMachine(employee);

        StateMachineEventResult<EmployeeState, EmployeeEvent> result =
            sendEvent(employee, stateMachine, event);

        assertThat(result.getResultType(), is(DENIED));
    }

    private StateMachineTestPlan<EmployeeState, EmployeeEvent> givenStateMachineTestPlan(
        StateMachine<EmployeeState, EmployeeEvent> stateMachine,
        Message<EmployeeEvent> eventMessage,
        EmployeeState state
    ) {
        return StateMachineTestPlanBuilder.<EmployeeState, EmployeeEvent>builder()
            .stateMachine(stateMachine)
            .step()
            .sendEvent(eventMessage)
            .expectState(state)
            .expectStateChanged(1)
            .and()
            .build();
    }

    private StateMachine<EmployeeState, EmployeeEvent> givenAddedStateMachine(Employee employee) {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = stateMachineFactory.getStateMachine();
        sendEvent(employee, stateMachine, ADD);
        return stateMachine;
    }

    private StateMachine<EmployeeState, EmployeeEvent> givenInCheckStateMachine(Employee employee) {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenAddedStateMachine(employee);
        sendEvent(employee, stateMachine, CHECK);
        return stateMachine;
    }

    private StateMachine<EmployeeState, EmployeeEvent> givenApprovedStateMachine(
        Employee employee) {
        StateMachine<EmployeeState, EmployeeEvent> stateMachine = givenInCheckStateMachine(
            employee);
        sendEvent(employee, stateMachine, APPROVE);
        return stateMachine;
    }

    private Message<EmployeeEvent> givenEmployeeEvent(Employee employee, EmployeeEvent event) {
        return MessageBuilder.withPayload(event)
            .setHeader(EMPLOYEE_HEADER, employee)
            .setHeader(EMPLOYEE_ID_HEADER, employee.getId())
            .build();
    }

    private StateMachineEventResult<EmployeeState, EmployeeEvent> sendEvent(Employee employee,
        StateMachine<EmployeeState, EmployeeEvent> stateMachine, EmployeeEvent event) {
        return stateMachine.sendEvent(Mono.just(givenEmployeeEvent(employee, event))).blockLast();
    }

    private Employee givenNewEmployee() {
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        return employee;
    }

    private Employee givenEmployee(Long id) {
        Employee employee = givenNewEmployee();
        employee.setId(id);
        return employee;
    }

}