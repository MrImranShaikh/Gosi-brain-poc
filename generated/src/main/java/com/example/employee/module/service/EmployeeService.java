import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
package com.example.employee.module.service;

public interface EmployeeService {
    
    List<Employee> getAllEmployees();
    
    Employee getEmployeeById(Long id);
    
    void saveEmployee(Employee employee);
    
    void updateEmployee(Long id, Employee employee);
    
    void deleteEmployee(Long id);
}