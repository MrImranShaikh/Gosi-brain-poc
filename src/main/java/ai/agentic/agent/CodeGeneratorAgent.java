package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class CodeGeneratorAgent {

    private final LLMClient llm;

    public CodeGeneratorAgent(LLMClient llm) {
        this.llm = llm;
    }

    public String generateEntity(String className, String packageName, String fieldsJson) {
        String prompt = """
            Generate a Java JPA entity.
            
            STRICT RULES:
            - Use Jakarta Persistence (jakarta.persistence)
            - Use Lombok for getters/setters
            - Always create a separate primary key field:
              - name: id
              - type: Long
              - annotations: @Id, @GeneratedValue(strategy = GenerationType.IDENTITY)
            - Business fields must NOT be annotated with @Id
            - Add @Column where appropriate
            - No explanations
            - Return ONLY Java code
            
            Class name: %s
            Package: %s
            Fields:
            %s
            """.formatted(className, packageName, fieldsJson);



        return llm.generate(prompt);
    }
    public String generateRepository(
            String entityName,
            String repositoryName,
            String packageName,
            String entityPackage
    ) {

        String prompt = """
Generate a Spring Data JPA repository.

STRICT RULES:
- Use Spring Data JPA
- Extend JpaRepository<%s, Long>
- Use correct imports
- No explanations
- Return ONLY Java code

Repository name: %s
Package: %s
Entity package: %s
""".formatted(entityName, repositoryName, packageName, entityPackage);

        return llm.generate(prompt);
    }

    public String generateServiceInterface(
            String serviceName,
            String packageName,
            String entityName
    ) {

        String prompt = """
Generate a service interface.

STRICT RULES:
- Pure interface
- Define basic CRUD methods
- Use %s as entity
- No implementation
- No explanations
- Return ONLY Java code

Service name: %s
Package: %s
""".formatted(entityName, serviceName, packageName);

        return llm.generate(prompt);
    }
    public String generateServiceImplementation(
            String implName,
            String packageName,
            String interfaceName,
            String interfacePackage,
            String repositoryName,
            String repositoryPackage,
            String entityName
    ) {

        String prompt = """
Generate a service implementation.

STRICT RULES:
- Annotate with @Service
- Implement %s
- Inject %s using constructor injection
- Implement CRUD methods
- No explanations
- Return ONLY Java code

Class name: %s
Package: %s
""".formatted(
                interfaceName,
                repositoryName,
                implName,
                packageName
        );

        return llm.generate(prompt);
    }
    public String generateController(
            String controllerName,
            String packageName,
            String serviceName,
            String servicePackage,
            String entityName
    ) {

        String prompt = """
Generate a REST controller.

STRICT RULES:
- Annotate with @RestController
- Base path: /api/%s
- Inject %s via constructor
- Provide CRUD endpoints
- Use ResponseEntity
- No explanations
- Return ONLY Java code

Controller name: %s
Package: %s
""".formatted(
                entityName.toLowerCase(),
                serviceName,
                controllerName,
                packageName
        );

        return llm.generate(prompt);
    }
    public String generateSpringBootMain(
            String className,
            String packageName
    ) {
        String prompt = """
Generate a Spring Boot main application class.

STRICT RULES:
- Use @SpringBootApplication
- Include main method
- No explanations
- Return ONLY Java code

Class name: %s
Package: %s
""".formatted(className, packageName);

        return llm.generate(prompt);
    }

}
