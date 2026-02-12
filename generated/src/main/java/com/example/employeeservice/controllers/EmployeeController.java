import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/employeeentity")
@Tag(name = "EmployeeEntity", description = "Employee Entity API")
public class EmployeeEntityController {

    @GetMapping
    @Operation(summary = "Get all employee entities", description = "Retrieves a list of all employee entities")
    public List<EmployeeEntity> getAll() {
        // implementation here
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee entity by ID", description = "Retrieves an employee entity by its ID")
    public EmployeeEntity getById(@PathVariable Long id) {
        // implementation here
    }

    @PostMapping
    @Operation(summary = "Create a new employee entity", description = "Creates a new employee entity")
    public EmployeeEntity create(@RequestBody EmployeeEntity employeeEntity) {
        // implementation here
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee entity", description = "Updates an existing employee entity by its ID")
    public EmployeeEntity update(@PathVariable Long id, @RequestBody EmployeeEntity employeeEntity) {
        // implementation here
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee entity", description = "Deletes an employee entity by its ID")
    public void delete(@PathVariable Long id) {
        // implementation here
    }
}