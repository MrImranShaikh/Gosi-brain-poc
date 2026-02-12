import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
package com.example.employeeservice.services;

public interface EmployeeService {
    List<EmployeeEntity> getAllEmployees();
    
    EmployeeEntity getEmployeeById(Long id);
    
    EmployeeEntity createEmployee(EmployeeEntity employee);
    
    void updateEmployee(Long id, EmployeeEntity employee);
    
    void deleteEmployee(Long id);
}