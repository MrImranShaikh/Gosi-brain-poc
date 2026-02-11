package ai.agentic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchitecturePlan {

    private Project project;
    private List<Dependency> dependencies;
    private Map<String, Boolean> layers;

    public boolean hasDependency(String dependencyName) {
        if (dependencies == null || dependencies.isEmpty()) {
            return false;
        }

        return dependencies.stream()
                .anyMatch(d ->
                        d.getName() != null &&
                                d.getName().equalsIgnoreCase(dependencyName)
                );
    }
}
