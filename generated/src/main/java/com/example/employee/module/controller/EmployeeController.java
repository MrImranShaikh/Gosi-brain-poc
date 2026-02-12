import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/employee")
@Tag(name = "Employee", description = "Employee API")
public class EmployeeController {

    // GET all
    @GetMapping
    @Operation(summary = "Get All Employees", description = "Retrieves a list of all employees")
    public List<Employee> getAllEmployees() {
        // implementation
    }

    // GET by id
    @GetMapping("/{id}")
    @Operation(summary = "Get Employee by ID", description = "Retrieves an employee by its ID")
    public Employee getEmployeeById(@PathVariable Long id) {
        // implementation
    }

    // POST create
    @PostMapping
    @Operation(summary = "Create Employee", description = "Creates a new employee")
    public Employee createEmployee(@RequestBody Employee employee) {
        // implementation
    }

    // PUT update
    @PutMapping("/{id}")
    @Operation(summary = "Update Employee", description = "Updates an existing employee")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        // implementation
    }

    // DELETE delete
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Employee", description = "Deletes an employee by its ID")
    public void deleteEmployee(@PathVariable Long id) {
        // implementation
    }
}