package ai.agentic.model;

import lombok.Data;
import java.util.List;

@Data
public class ModulePlan {

    private String moduleName;
    private String basePackage;

    private List<PlannedClass> entities;
    private List<PlannedClass> repositories;
    private List<PlannedClass> serviceInterfaces;
    private List<PlannedClass> serviceImplementations;
    private List<PlannedClass> controllers;
}
