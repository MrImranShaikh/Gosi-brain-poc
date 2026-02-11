package ai.agentic;

import ai.agentic.agent.*;
import ai.agentic.cli.GenerationTarget;
import ai.agentic.fs.JavaFileWriter;
import ai.agentic.fs.MavenWrapperGenerator;
import ai.agentic.fs.PlantUmlFileWriter;
import ai.agentic.fs.PomTemplate;
import ai.agentic.llm.LLMClient;
import ai.agentic.llm.OllamaClient;
import ai.agentic.model.ArchitecturePlan;
import ai.agentic.model.ModulePlan;
import ai.agentic.model.PlannedClass;
import ai.agentic.validation.ValidatorAgent;
import ai.agentic.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class App {

    public static void main(String[] args) throws IOException {
        System.out.println("Args length: " + args.length);
        for (String arg : args) {
            System.out.println("Arg: " + arg);
        }
        if (args.length == 0) {
            System.out.println("""
                Usage:
                  agentic <brd-file> [-t target]

                Targets:
                  erd           Architecture / ERD-level view
                  entity        JPA entities
                  repository    Spring Data repositories
                  service       Service interfaces + implementations
                  controller    REST controllers
                  code          Full Spring Boot project (default)

                Examples:
                  agentic brd.txt
                  agentic brd.txt -t entity
                  agentic brd.txt -t service --dry-run
                """);
            return;
        }

        // ---------- CLI ----------
        Path brdPath = Path.of(args[0]);
        Path outputDir = brdPath.getParent() != null
                ? brdPath.getParent().resolve("generated")
                : Path.of("generated");

        for (int i = 1; i < args.length; i++) {
            if (("-o".equals(args[i]) || "--out".equals(args[i])) && i + 1 < args.length) {
                outputDir = Path.of(args[i + 1]);
                i++;
            }
        }

        GenerationTarget target = GenerationTarget.CODE;

        boolean dryRun = false;
        boolean plantUml = false;

        for (int i = 1; i < args.length; i++) {
            if ("--plantuml".equals(args[i])) {
                plantUml = true;
            }
            if ("--dry-run".equals(args[i])) {
                dryRun = true;
            }

            if ("-t".equals(args[i]) && i + 1 < args.length) {
                target = GenerationTarget.from(args[i + 1]);
                i++;
            }
        }

        Path generated = Path.of(outputDir.toString());
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Starting agentic code generation...");

        String brd = Files.readString(brdPath).replace("\r\n", "\n");

        LLMClient llm = new OllamaClient("llama3");

        // ---------- 1️ Requirement analysis ----------
        RequirementAnalyzerAgent reqAgent = new RequirementAnalyzerAgent(llm);
        String requirementJson = reqAgent.analyze(brd);

        // ---------- 2️ Architecture planning ----------
        ArchitecturePlannerAgent archAgent = new ArchitecturePlannerAgent(llm);
        String architectureJson = archAgent.planArchitecture(requirementJson);



        ArchitecturePlan architecturePlan =
                mapper.readValue(architectureJson, ArchitecturePlan.class);

        // ---------- 3️ Module planning ----------
        ModulePlannerAgent moduleAgent = new ModulePlannerAgent(llm);
        String rawModulePlan = moduleAgent.planModule(requirementJson, architectureJson);
        String modulePlanJson = extractJsonObject(rawModulePlan);

        ModulePlan modulePlan =
                mapper.readValue(modulePlanJson, ModulePlan.class);
        validateModulePlan(modulePlan);

        System.out.println("Parsed module: " + modulePlan.getModuleName());

        if (target == GenerationTarget.ERD) {
            PlantUmlAgent umlAgent = new PlantUmlAgent(llm);
            PlantUmlFileWriter umlWriter =
                    new PlantUmlFileWriter(outputDir.toString(), false);

            String erd = umlAgent.generateERD(
                    architectureJson,
                    modulePlanJson
            );

            umlWriter.writeDiagram(
                    modulePlan.getModuleName() + "-ERD",
                    erd
            );

            System.out.println("ERD generation complete.");
            return;
        }
        // ---------- TARGET SHORT-CIRCUITS ----------
        if (target == GenerationTarget.ENTITY) {
            System.out.println("===== ENTITIES =====");
            modulePlan.getEntities().forEach(e ->
                    System.out.println(e.getPackageName() + "." + e.getName()));
            return;
        }

        if (target == GenerationTarget.REPOSITORY) {
            System.out.println("===== REPOSITORIES =====");
            modulePlan.getRepositories().forEach(r ->
                    System.out.println(r.getPackageName() + "." + r.getName()));
            return;
        }

        if (target == GenerationTarget.SERVICE) {
            System.out.println("===== SERVICES =====");

            System.out.println("-- Interfaces --");
            modulePlan.getServiceInterfaces().forEach(s ->
                    System.out.println(s.getPackageName() + "." + s.getName()));

            System.out.println("-- Implementations --");
            modulePlan.getServiceImplementations().forEach(s ->
                    System.out.println(s.getPackageName() + "." + s.getName()));
            return;
        }

        if (target == GenerationTarget.CONTROLLER) {
            System.out.println("===== CONTROLLERS =====");
            modulePlan.getControllers().forEach(c ->
                    System.out.println(c.getPackageName() + "." + c.getName()));
            return;
        }

        // ---------- 4️ CODE GENERATION ----------
        JavaFileWriter writer =
                new JavaFileWriter(outputDir.toString(), dryRun);

        CodeGeneratorAgent codeGen = new CodeGeneratorAgent(llm);

        PlannedClass entity = modulePlan.getEntities().get(0);

        String fieldsJson = """
        [
          {"name": "name", "type": "string"},
          {"name": "email", "type": "string"}
        ]
        """;

        writePlannedClass(writer, entity,
                codeGen.generateEntity(
                        entity.getName(),
                        entity.getPackageName(),
                        fieldsJson));

        PlannedClass repository = modulePlan.getRepositories().get(0);
        writePlannedClass(writer, repository,
                codeGen.generateRepository(
                        entity.getName(),
                        repository.getName(),
                        repository.getPackageName(),
                        entity.getPackageName()));

        PlannedClass serviceInterface = modulePlan.getServiceInterfaces().get(0);
        writePlannedClass(writer, serviceInterface,
                codeGen.generateServiceInterface(
                        serviceInterface.getName(),
                        serviceInterface.getPackageName(),
                        entity.getName()));

        PlannedClass serviceImpl = modulePlan.getServiceImplementations().get(0);
        writePlannedClass(writer, serviceImpl,
                codeGen.generateServiceImplementation(
                        serviceImpl.getName(),
                        serviceImpl.getPackageName(),
                        serviceInterface.getName(),
                        serviceInterface.getPackageName(),
                        repository.getName(),
                        repository.getPackageName(),
                        entity.getName()));

        PlannedClass controller = modulePlan.getControllers().get(0);
        writePlannedClass(writer, controller,
                codeGen.generateController(
                        controller.getName(),
                        controller.getPackageName(),
                        serviceInterface.getName(),
                        serviceInterface.getPackageName(),
                        entity.getName()));

        // ---------- pom.xml ----------
        boolean web = architecturePlan.hasDependency("spring-boot-starter-web");
        boolean jpa = architecturePlan.hasDependency("spring-boot-starter-data-jpa");
        boolean validation =
                architecturePlan.hasDependency("spring-boot-starter-validation")
                        || architecturePlan.hasDependency("hibernate-validator");

        String pomXml = PomTemplate.render(
                architecturePlan.getProject().getBasePackage(),
                modulePlan.getModuleName(),
                "0.0.1-SNAPSHOT",
                architecturePlan.getProject().getJavaVersion(),
                "3.2.2",
                web,
                jpa,
                validation
        );

        Files.writeString(generated.resolve("pom.xml"), pomXml);
        MavenWrapperGenerator.generate(generated);

        // ---------- Spring Boot main ----------
        String appClassName = modulePlan.getModuleName() + "Application";
        writer.writeJavaFile(
                modulePlan.getBasePackage(),
                appClassName,
                codeGen.generateSpringBootMain(
                        appClassName,
                        modulePlan.getBasePackage())
        );

        // ---------- 5️⃣ Validation ----------
        ValidatorAgent validator = new ValidatorAgent();
        ValidationResult result = validator.validate(generated);

        System.out.println(result.isSuccess() ? "BUILD SUCCESS" : "BUILD FAILED");
        if (!result.isSuccess()) {
            System.out.println(result.getOutput());
        }
    }

    // ================= HELPERS =================

    private static void writePlannedClass(
            JavaFileWriter writer,
            PlannedClass plannedClass,
            String code
    ) {
        writer.writeJavaFile(
                plannedClass.getPackageName(),
                plannedClass.getName(),
                sanitizeJavaCode(code)
        );
    }

    private static String sanitizeJavaCode(String code) {
        String cleaned = code.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(cleaned.indexOf('\n') + 1);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.lastIndexOf("```")).trim();
        }
        return cleaned;
    }

    private static String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalStateException("Invalid JSON from agent:\n" + text);
        }
        return text.substring(start, end + 1);
    }

    private static void validateModulePlan(ModulePlan plan) {
        if (plan.getEntities().isEmpty()
                || plan.getRepositories().isEmpty()
                || plan.getServiceInterfaces().isEmpty()
                || plan.getServiceImplementations().isEmpty()
                || plan.getControllers().isEmpty()) {
            throw new IllegalStateException("Invalid ModulePlan: missing layers");
        }
    }
}
