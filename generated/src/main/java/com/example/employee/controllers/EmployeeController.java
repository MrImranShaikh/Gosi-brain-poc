package com.example.employee.controllers;

import com.example.employee.entities.EmployeeEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/employeeentity")
@Tag(name = "EmployeeEntity", description = "Employee Entity API")

public class EmployeeController {

    // GET all
    @GetMapping
    @Operation(summary = "Get all employee entities", description = "Returns a list of all employee entities")
    public List<EmployeeEntity> getAll() {
        // Implementation
        return null;
    }

    // GET by id
    @GetMapping("/{id}")
    @Operation(summary = "Get an employee entity by ID", description = "Returns an employee entity by its ID")
    public EmployeeEntity getById(@PathVariable Long id) {
        // Implementation
        return null;
    }

    // POST create
    @PostMapping
    @Operation(summary = "Create a new employee entity", description = "Creates a new employee entity")
    public HttpStatus create(@RequestBody EmployeeEntity employeeEntity) {
        // Implementation
        return null;
    }

    // PUT update
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee entity", description = "Updates an existing employee entity")
    public HttpStatus update(@PathVariable Long id, @RequestBody EmployeeEntity employeeEntity) {
        // Implementation
        return null;
    }

    // DELETE delete
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee entity", description = "Deletes an employee entity by its ID")
    public HttpStatus delete(@PathVariable Long id) {
        // Implementation
        return null;
    }
}