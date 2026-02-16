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
            String servicePackage,
            String entityName,
            String entityPackage) {

        String prompt = """
        Generate a Java Spring Boot service interface.

        Rules:
        - Do NOT add explanation text.
        - Do NOT add markdown.
        - Do NOT generate package statement.
        - Do NOT invent imports.
        - Use EXACT entity import provided below.

        Required Import:
        import %s.%s;

        Generate CRUD methods:
        - List<%s> findAll();
        - %s findById(Long id);
        - %s create(%s entity);
        - %s update(Long id, %s entity);
        - void delete(Long id);

        Only output valid Java interface code.
        """.formatted(
                entityPackage,
                entityName,
                entityName,
                entityName,
                entityName,
                entityName,
                entityName,
                entityName
        );

        return llm.generate(prompt);
    }

    public String generateServiceImplementation(
            String implName,
            String implPackage,
            String interfaceName,
            String interfacePackage,
            String entityName,
            String entityPackage,
            String repositoryName,
            String repositoryPackage) {

        String prompt = """
        Generate a Java Spring Boot service implementation.

        Rules:
        - Do NOT add explanation text.
        - Do NOT add markdown.
        - Do NOT generate package statement.
        - Use EXACT imports provided.
        - Implement interface EXACTLY.
        - Add @Service annotation.

        Required Imports:
        import %s.%s;
        import %s.%s;
        import %s.%s;

        Implement interface: %s

        Generate full CRUD implementation.
        """.formatted(
                entityPackage,
                entityName,
                repositoryPackage,
                repositoryName,
                interfacePackage,
                interfaceName,
                interfaceName
        );

        return llm.generate(prompt);
    }
    public String generateController(
            String controllerName,
            String controllerPackage,
            String serviceName,
            String servicePackage,
            String entityName,
            String entityPackage,
            String contract) {

        String prompt = """
            You are generating pure Java source code.
            
            STRICT RULES:
            - Output ONLY valid Java code.
            - Do NOT include explanations.
            - Do NOT include markdown.
            - Do NOT include package statement.
            - The first line MUST start with an import or annotation.
            - You MUST call the service using EXACTLY the following method signatures.
            - Do NOT invent method names.
            - Do NOT rename methods.
            - Do NOT add extra methods.
            
            Service Contract:
            %s
            
            Class Details:
            Controller Name: %s
            Service: %s
            Service Package: %s
            Entity: %s
            Entity Package: %s
            
            Required Imports:
            import %s.%s;
            import %s.%s;
            import org.springframework.web.bind.annotation.*;
            import org.springframework.http.ResponseEntity;
            import org.springframework.http.HttpStatus;
            import java.util.List;
            import io.swagger.v3.oas.annotations.Operation;
            
            Generate a Spring Boot REST controller using:
            - @RestController
            - @RequestMapping("/api/v1/%s")
            - Constructor injection
            - CRUD endpoints mapped directly to the service contract above
            """.formatted(
                            contract,
                            controllerName,
                            serviceName,
                            servicePackage,
                            entityName,
                            entityPackage,
                            entityPackage,
                            entityName,
                            servicePackage,
                            serviceName,
                            entityName.toLowerCase()
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
            - Do not change the casing for the class name or package name
            Class name: %s
            Package: %s
            """.formatted(className, packageName);

        return llm.generate(prompt);
    }
    public String generateServiceImplementationFromContract(
            String implName,
            String implPackage,
            String interfaceName,
            String interfacePackage,
            String entityName,
            String entityPackage,
            String repositoryName,
            String repositoryPackage,
            String contract
    ) {

        String prompt = """
You are generating source code.

Return ONLY valid Java code.
Do NOT include explanations.
Do NOT include comments outside the class.
Do NOT include markdown.
Do NOT include any extra text.

Generate a Spring Boot service implementation.

Class name: %s
Package: %s
Implements: %s
Interface package: %s
Entity: %s
Entity package: %s
Repository: %s
Repository package: %s

You MUST implement ALL of the following methods exactly:

%s

Rules:
- Use @Service
- Inject repository via constructor
- Do not rename methods
- Do not change parameters
- No additional methods
- No explanations
""".formatted(
                implName,
                implPackage,
                interfaceName,
                interfacePackage,
                entityName,
                entityPackage,
                repositoryName,
                repositoryPackage,
                contract
        );

        return llm.generate(prompt);
    }

}
