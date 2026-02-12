import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

package com.example.employee.services;

import java.util.List;
import com.example.employee.entities.EmployeeEntity;

public interface EmployeeService {
    List<EmployeeEntity> getAllEmployees();
    
    EmployeeEntity getEmployeeById(Long id);
    
    void saveEmployee(EmployeeEntity employee);
    
    void updateEmployee(Long id, EmployeeEntity employee);
    
    void deleteEmployee(Long id);
}