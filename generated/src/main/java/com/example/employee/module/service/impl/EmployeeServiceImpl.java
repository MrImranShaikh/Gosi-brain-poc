import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
package com.example.employee.module.service.impl;

import com.example.employee.module.repository.EmployeeRepository;
import com.example.employee.module.service.EmployeeService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, Employee employee) {
        return employeeRepository.findById(id)
                .map(e -> {
                    e.setName(employee.getName());
                    e.setJobTitle(employee.getJobTitle());
                    return employeeRepository.save(e);
                })
                .orElseGet(() -> employeeRepository.save(employee));
    }

    @Override
    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }
}