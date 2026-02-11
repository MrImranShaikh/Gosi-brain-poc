package com.example.employee.service;

import java.util.List;
import com.example.employee.domain.Employee;

public interface EmployeeService {
    List<Employee> getAllEmployees();

    Employee getEmployee(Long id);

    Employee createEmployee(Employee employee);

    Employee updateEmployee(Long id, Employee employee);

    void deleteEmployee(Long id);
}