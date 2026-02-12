package com.example.employee.services;

import com.example.employee.entities.EmployeeEntity;

import java.util.List;

public interface EmployeeService {
    List<EmployeeEntity> getAllEmployees();
    EmployeeEntity getEmployeeById(Long id);
    EmployeeEntity createEmployee(EmployeeEntity employee);
    EmployeeEntity updateEmployee(EmployeeEntity employee);
    void deleteEmployee(Long id);
}