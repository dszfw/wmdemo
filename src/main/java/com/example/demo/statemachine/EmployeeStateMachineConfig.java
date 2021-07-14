package com.example.demo.statemachine;

import static com.example.demo.statemachine.EmployeeEvent.ADD;
import static com.example.demo.statemachine.EmployeeEvent.APPROVE;
import static com.example.demo.statemachine.EmployeeEvent.CHECK;
import static com.example.demo.statemachine.EmployeeState.ACTIVE;
import static com.example.demo.statemachine.EmployeeState.ADDED;
import static com.example.demo.statemachine.EmployeeState.APPROVED;
import static com.example.demo.statemachine.EmployeeState.INIT;
import static com.example.demo.statemachine.EmployeeState.IN_CHECK;

import com.example.demo.service.EmployeeServiceImpl;
import com.example.demo.statemachine.action.ActivateEmployeeAction;
import com.example.demo.statemachine.action.AddEmployeeAction;
import com.example.demo.statemachine.action.ApproveEmployeeAction;
import com.example.demo.statemachine.action.CheckEmployeeAction;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

@Configuration
@EnableStateMachineFactory
public class EmployeeStateMachineConfig
    extends EnumStateMachineConfigurerAdapter<EmployeeState, EmployeeEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final AddEmployeeAction addEmployeeAction;
    private final CheckEmployeeAction checkEmployeeAction;
    private final ApproveEmployeeAction approveEmployeeAction;
    private final ActivateEmployeeAction activateEmployeeAction;

    public EmployeeStateMachineConfig(AddEmployeeAction addEmployeeAction,
        CheckEmployeeAction checkEmployeeAction,
        ApproveEmployeeAction approveEmployeeAction,
        ActivateEmployeeAction activateEmployeeAction) {
        this.addEmployeeAction = addEmployeeAction;
        this.checkEmployeeAction = checkEmployeeAction;
        this.approveEmployeeAction = approveEmployeeAction;
        this.activateEmployeeAction = activateEmployeeAction;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<EmployeeState, EmployeeEvent> config)
        throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<EmployeeState, EmployeeEvent> states)
        throws Exception {
        states
            .withStates()
            .initial(INIT)
            .states(EnumSet.allOf(EmployeeState.class));
    }

    @Override
    public void configure(
        StateMachineTransitionConfigurer<EmployeeState, EmployeeEvent> transitions)
        throws Exception {
        transitions
            .withExternal()
            .source(INIT).target(ADDED).event(ADD)
            .action(addEmployeeAction)

            .and()
            .withExternal()
            .source(ADDED).target(IN_CHECK).event(CHECK)
            .action(checkEmployeeAction)

            .and()
            .withExternal()
            .source(IN_CHECK).target(APPROVED).event(APPROVE)
            .action(approveEmployeeAction)

            .and()
            .withExternal()
            .source(APPROVED).target(ACTIVE).event(EmployeeEvent.ACTIVATE)
            .action(activateEmployeeAction);
    }

    @Bean
    public StateMachineListener<EmployeeState, EmployeeEvent> listener() {
        return new StateMachineListenerAdapter<EmployeeState, EmployeeEvent>() {
            @Override
            public void eventNotAccepted(Message<EmployeeEvent> event) {
                LOG.warn("Event {} was not accepted", event.getPayload());
            }
        };
    }

    @Bean
    public StateMachinePersister<EmployeeState, EmployeeEvent, String> persister() {
        return new DefaultStateMachinePersister<>(new EmployeeStateMachinePersist());
    }

}