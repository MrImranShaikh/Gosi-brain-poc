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
import ai.agentic.util.ServiceContractUtil;
import ai.agentic.validation.ValidatorAgent;
import ai.agentic.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

public class App {

    public static void main(String[] args) {

        try {

            if (args.length == 0) {
                printUsage();
                return;
            }

            Path brdPath = Path.of(args[0]).toAbsolutePath();

            if (!Files.exists(brdPath)) {
                System.err.println("BRD file not found: " + brdPath);
                return;
            }

            Path outputDir = null;
            boolean customOutputProvided = false;
            GenerationTarget target = GenerationTarget.CODE;
            boolean dryRun = false;

            String artifactRoot = "com.example";
            String projectVersion = "0.0.1-SNAPSHOT";
            String springBootVersion = "3.2.2";
            String database = "none";
            boolean swaggerEnabled = true;

            // ---------- CLI ----------
            for (int i = 1; i < args.length; i++) {

                if (("-o".equals(args[i]) || "--out".equals(args[i])) && i + 1 < args.length) {
                    outputDir = Path.of(args[i + 1]).toAbsolutePath();
                    customOutputProvided = true;
                    i++;
                    continue;
                }

                if ("-a".equals(args[i]) && i + 1 < args.length) {
                    artifactRoot = args[i + 1].trim();
                    i++;
                    continue;
                }

                if ("-v".equals(args[i]) && i + 1 < args.length) {
                    projectVersion = args[i + 1].trim();
                    i++;
                    continue;
                }

                if ("--boot".equals(args[i]) && i + 1 < args.length) {
                    springBootVersion = args[i + 1].trim();
                    i++;
                    continue;
                }

                if ("--db".equals(args[i]) && i + 1 < args.length) {
                    database = args[i + 1].toLowerCase().trim();
                    i++;
                    continue;
                }

                if ("--swagger".equals(args[i]) && i + 1 < args.length) {
                    if ("off".equalsIgnoreCase(args[i + 1])) {
                        swaggerEnabled = false;
                    }
                    i++;
                    continue;
                }

                if ("--dry-run".equals(args[i])) {
                    dryRun = true;
                    continue;
                }

                if ("-t".equals(args[i]) && i + 1 < args.length) {
                    target = GenerationTarget.from(args[i + 1]);
                    i++;
                }
            }

            artifactRoot = artifactRoot.toLowerCase();

            if (!artifactRoot.matches("^[a-zA-Z0-9_.]+$")) {
                throw new IllegalArgumentException("Invalid groupId: " + artifactRoot);
            }

            ObjectMapper mapper = new ObjectMapper();
            String brd = Files.readString(brdPath).replace("\r\n", "\n");

            LLMClient llm = new OllamaClient("llama3");

            RequirementAnalyzerAgent reqAgent = new RequirementAnalyzerAgent(llm);
            String requirementJson = reqAgent.analyze(brd);

            ArchitecturePlannerAgent archAgent = new ArchitecturePlannerAgent(llm);
            String architectureJson = archAgent.planArchitecture(requirementJson);

            ArchitecturePlan architecturePlan =
                    mapper.readValue(architectureJson, ArchitecturePlan.class);

            ModulePlannerAgent moduleAgent = new ModulePlannerAgent(llm);
            String rawModulePlan = moduleAgent.planModule(requirementJson, architectureJson);
            String modulePlanJson = extractJsonObject(rawModulePlan);

            ModulePlan modulePlan =
                    mapper.readValue(modulePlanJson, ModulePlan.class);

            String cleanModuleName = modulePlan.getModuleName()
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .toLowerCase();

            String basePackage = artifactRoot + "." + cleanModuleName;
            modulePlan.setBasePackage(basePackage);
            rewritePackages(modulePlan, basePackage);

            if (!customOutputProvided) {
                outputDir = brdPath.getParent() != null
                        ? brdPath.getParent().resolve(modulePlan.getModuleName())
                        : Path.of(modulePlan.getModuleName());
            }

            Files.createDirectories(outputDir);

            if (target == GenerationTarget.ERD) {
                generateERD(llm, architectureJson, modulePlanJson, modulePlan, outputDir);
                return;
            }

            JavaFileWriter writer = new JavaFileWriter(outputDir.toString(), dryRun);
            CodeGeneratorAgent codeGen = new CodeGeneratorAgent(llm);

            PlannedClass entity = modulePlan.getEntities().get(0);

            for (PlannedClass e : modulePlan.getEntities()) {
                write(writer, e, codeGen.generateEntity(
                        e.getName(),
                        e.getPackageName(),
                        defaultFieldsJson()));
            }

            for (PlannedClass repo : modulePlan.getRepositories()) {
                write(writer, repo,
                        codeGen.generateRepository(
                                entity.getName(),
                                repo.getName(),
                                repo.getPackageName(),
                                entity.getPackageName()));
            }

            PlannedClass serviceInterface = modulePlan.getServiceInterfaces().get(0);

            String interfaceCode = codeGen.generateServiceInterface(
                    serviceInterface.getName(),
                    serviceInterface.getPackageName(),
                    entity.getName(),
                    entity.getPackageName()
            );

            write(writer, serviceInterface, interfaceCode);

            String contract =
                    ServiceContractUtil.extractMethodSignatures(interfaceCode);

            for (PlannedClass impl : modulePlan.getServiceImplementations()) {

                String implCode =
                        codeGen.generateServiceImplementationFromContract(
                                impl.getName(),
                                impl.getPackageName(),
                                serviceInterface.getName(),
                                serviceInterface.getPackageName(),
                                entity.getName(),
                                entity.getPackageName(),
                                modulePlan.getRepositories().get(0).getName(),
                                modulePlan.getRepositories().get(0).getPackageName(),
                                contract
                        );

                write(writer, impl, implCode);
            }



            for (PlannedClass controller : modulePlan.getControllers()) {
                write(writer, controller,
                        codeGen.generateController(
                                controller.getName(),
                                controller.getPackageName(),
                                serviceInterface.getName(),
                                serviceInterface.getPackageName(),
                                entity.getName(),
                                entity.getPackageName(),
                                contract
                                ));
            }

            String pomXml = PomTemplate.render(
                    artifactRoot,
                    cleanModuleName,
                    projectVersion,
                    architecturePlan.getProject().getJavaVersion(),
                    springBootVersion,
                    architecturePlan.hasDependency("spring-boot-starter-web"),
                    true,
                    architecturePlan.hasDependency("spring-boot-starter-validation"),
                    database,
                    swaggerEnabled
            );

            Files.writeString(outputDir.resolve("pom.xml"), pomXml);

            // ---------- MAIN APPLICATION ----------
            String appClassName =
                    Character.toUpperCase(cleanModuleName.charAt(0))
                            + cleanModuleName.substring(1)
                            + "Application";

            String mainClassCode =
                    codeGen.generateSpringBootMain(
                            appClassName,
                            basePackage
                    );

            write(writer,
                    new PlannedClass(appClassName, basePackage),
                    mainClassCode
            );

            MavenWrapperGenerator.generate(outputDir);

            ValidatorAgent validator = new ValidatorAgent();
            ValidationResult result = validator.validate(outputDir);

            if (result.isSuccess()) {
                System.out.println("BUILD SUCCESS");
            } else {
                System.out.println("BUILD FAILED");
                System.out.println(result.getOutput());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void write(JavaFileWriter writer, PlannedClass cls, String code) {

        if (code == null) {
            code = "";
        }

        String cleaned = code.trim()
                .replace("```java", "")
                .replace("```", "");

        // Remove any existing package declarations
        cleaned = cleaned.replaceAll("package\\s+[^;]+;", "");

        // Remove explanation text before first valid Java construct
        int importIndex = cleaned.indexOf("import ");
        int annotationIndex = cleaned.indexOf("@");
        int classIndex = cleaned.indexOf("public class");
        int interfaceIndex = cleaned.indexOf("public interface");

        int firstValid = minPositive(importIndex, annotationIndex, classIndex, interfaceIndex);

        if (firstValid > 0) {
            cleaned = cleaned.substring(firstValid);
        }

        if (cleaned.contains("ResponseEntity") && !cleaned.contains("import org.springframework.http.ResponseEntity")) {
            cleaned = "import org.springframework.http.ResponseEntity;\n" + cleaned;
        }

        if (cleaned.contains("HttpStatus") && !cleaned.contains("import org.springframework.http.HttpStatus")) {
            cleaned = "import org.springframework.http.HttpStatus;\n" + cleaned;
        }

        String finalCode =
                "package " + cls.getPackageName() + ";\n\n" +
                        cleaned.trim();

        writer.writeJavaFile(
                cls.getPackageName(),
                cls.getName(),
                finalCode
        );
    }


    private static String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return text.substring(start, end + 1);
    }

    private static String defaultFieldsJson() {
        return """
        [
          {"name": "name", "type": "string"},
          {"name": "email", "type": "string"}
        ]
        """;
    }

    private static void printUsage() {
        System.out.println("""
Usage:
  agentic <brd-file> [options]

Options:
  -a <groupId>
  -v <version>
  --boot <bootVersion>
  --db mysql|postgres|h2|none
  --swagger off
  -o <outputDir>
""");
    }

    private static void generateERD(
            LLMClient llm,
            String architectureJson,
            String modulePlanJson,
            ModulePlan modulePlan,
            Path outputDir) {

        PlantUmlAgent umlAgent = new PlantUmlAgent(llm);
        PlantUmlFileWriter umlWriter =
                new PlantUmlFileWriter(outputDir.toString(), false);

        String erd = umlAgent.generateERD(architectureJson, modulePlanJson);
        umlWriter.writeDiagram(modulePlan.getModuleName() + "-ERD", erd);
    }
    private static void rewritePackages(ModulePlan modulePlan, String basePackage) {

        modulePlan.getEntities().forEach(c ->
                c.setPackageName(basePackage + ".entities"));

        modulePlan.getRepositories().forEach(c ->
                c.setPackageName(basePackage + ".repositories"));

        modulePlan.getServiceInterfaces().forEach(c ->
                c.setPackageName(basePackage + ".services"));

        modulePlan.getServiceImplementations().forEach(c ->
                c.setPackageName(basePackage + ".services.impl"));

        modulePlan.getControllers().forEach(c ->
                c.setPackageName(basePackage + ".controllers"));
    }
    private static int minPositive(int... values) {
        int min = Integer.MAX_VALUE;
        for (int v : values) {
            if (v >= 0 && v < min) {
                min = v;
            }
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }

}
