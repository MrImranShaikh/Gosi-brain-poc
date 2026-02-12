import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employeeentity")
@Tag(name = "EmployeeEntity", description = "CRUD operations for EmployeeEntity")

public class EmployeeEntityController {

    @GetMapping
    @Operation(summary = "Get all EmployeeEntities", description = "Returns a list of all EmployeeEntities")
    public ResponseEntity<List<EmployeeEntity>> getAll() {
        // Your implementation here
        return new ResponseEntity<>(List.of(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get EmployeeEntity by ID", description = "Returns an EmployeeEntity by its ID")
    public ResponseEntity<EmployeeEntity> getById(@PathVariable Long id) {
        // Your implementation here
        return new ResponseEntity<>(new EmployeeEntity(), HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a new EmployeeEntity", description = "Creates a new EmployeeEntity and returns it")
    public ResponseEntity<EmployeeEntity> create(@RequestBody EmployeeEntity employeeEntity) {
        // Your implementation here
        return new ResponseEntity<>(employeeEntity, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing EmployeeEntity", description = "Updates an existing EmployeeEntity and returns it")
    public ResponseEntity<EmployeeEntity> update(@PathVariable Long id, @RequestBody EmployeeEntity employeeEntity) {
        // Your implementation here
        return new ResponseEntity<>(employeeEntity, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an EmployeeEntity by ID", description = "Deletes an EmployeeEntity by its ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Your implementation here
        return ResponseEntity.noContent().build();
    }
}