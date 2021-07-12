package com.example.demo.service;

import com.example.demo.model.Employee;

public interface EmployeeService {

    Employee add(Employee employee);

    Employee save(Employee employee);

    Employee findById(Long id);

    void check(Long employeeId);

    void approve(Long employeeId);

    void activate(Long employeeId);

    boolean exists(Long employeeId);

}
