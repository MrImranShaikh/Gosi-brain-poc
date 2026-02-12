import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/employeeentity")
@Tag(name = "EmployeeEntity API", description = "REST endpoints for EmployeeEntity operations")
public class EmployeeController {

    @Operation(summary = "Get all employee entities", tags = {"EmployeeEntity"})
    @GetMapping
    public List<EmployeeEntity> getAll() {
        // implementation
    }

    @Operation(summary = "Get an employee entity by id", tags = {"EmployeeEntity"})
    @GetMapping("/{id}")
    public EmployeeEntity getById(@PathVariable Long id) {
        // implementation
    }

    @Operation(summary = "Create a new employee entity", tags = {"EmployeeEntity"})
    @PostMapping
    public EmployeeEntity create(@RequestBody EmployeeEntity entity) {
        // implementation
    }

    @Operation(summary = "Update an existing employee entity", tags = {"EmployeeEntity"})
    @PutMapping("/{id}")
    public EmployeeEntity update(@PathVariable Long id, @RequestBody EmployeeEntity entity) {
        // implementation
    }

    @Operation(summary = "Delete an employee entity by id", tags = {"EmployeeEntity"})
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // implementation
    }
}