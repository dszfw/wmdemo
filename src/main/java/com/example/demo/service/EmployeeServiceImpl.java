package com.example.demo.service;

import static com.example.demo.statemachine.EmployeeState.ACTIVE;
import static com.example.demo.statemachine.EmployeeState.ADDED;
import static com.example.demo.statemachine.EmployeeState.APPROVED;
import static com.example.demo.statemachine.EmployeeState.IN_CHECK;

import com.example.demo.model.Employee;
import com.example.demo.repo.EmployeeRepository;
import com.example.demo.statemachine.EmployeeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee add(Employee employee) {
        LOG.info("Add new employee");
        employee.setState(ADDED);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElseThrow(
            () -> new RuntimeException(String.format("Employee with id = %d was not found", id)));
    }

    @Override
    public void check(Long employeeId) {
        LOG.info("Check employee, id = {}", employeeId);
        changeState(findById(employeeId), IN_CHECK);
    }

    @Override
    public void approve(Long employeeId) {
        LOG.info("Approve employee, id = {}", employeeId);
        changeState(findById(employeeId), APPROVED);
    }

    @Override
    public void activate(Long employeeId) {
        LOG.info("Activate employee, id = {}", employeeId);
        changeState(findById(employeeId), ACTIVE);
    }

    @Override
    public boolean exists(Long employeeId) {
        return employeeRepository.existsById(employeeId);
    }

    private void changeState(Employee employee, EmployeeState state) {
        employee.setState(state);
        employeeRepository.save(employee);
    }

}
