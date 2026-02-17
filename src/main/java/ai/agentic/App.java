package ai.agentic;

import ai.agentic.agent.*;
import ai.agentic.cli.GenerationTarget;
import ai.agentic.fs.*;
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

import static ai.agentic.agent.LanguageTranslatorAgent.translatePreservingFormat;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class App {

    public static void main(String[] args) {

        try {
            String translationLanguage = null;

            if (args.length == 0 ||
                    "-h".equalsIgnoreCase(args[0]) ||
                    "--help".equalsIgnoreCase(args[0]) ||
                    "help".equalsIgnoreCase(args[0])) {

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
            DatabaseType databaseType = DatabaseType.NONE;

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
                    databaseType = DatabaseType.from(args[i + 1]);
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

                if ("--translate".equals(args[i]) && i + 1 < args.length) {
                    translationLanguage = args[i + 1];
                    i++;
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
            LLMClient llm = new OllamaClient("llama3");

            if (translationLanguage != null) {
                String content = readBrdFile(brdPath);

                String translated = translatePreservingFormat(llm, content, translationLanguage);

                Path output = brdPath.getParent()
                        .resolve(removeExtension(brdPath.getFileName().toString())
                                + "_" + translationLanguage + ".txt");

                Files.writeString(output, translated);

                System.out.println("Translation complete: " + output);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            String brd = readBrdFile(brdPath);



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
            boolean jpaEnabled =
                    architecturePlan.hasDependency("spring-boot-starter-data-jpa");

            if (databaseType != DatabaseType.NONE && !jpaEnabled) {
                System.out.println("Database selected but JPA not enabled. Enabling JPA.");
                jpaEnabled = true;
            }

            if (jpaEnabled && databaseType == DatabaseType.NONE) {
                System.out.println("JPA detected but no database selected. Defaulting to H2.");
                databaseType = DatabaseType.H2;
            }

            if (jpaEnabled && databaseType == DatabaseType.NONE) {
                throw new IllegalStateException(
                        "Invalid configuration: JPA requires a database."
                );
            }

            String pomXml = PomTemplate.render(
                    artifactRoot,
                    cleanModuleName,
                    projectVersion,
                    architecturePlan.getProject().getJavaVersion(),
                    springBootVersion,
                    architecturePlan.hasDependency("spring-boot-starter-web"),
                    jpaEnabled,
                    architecturePlan.hasDependency("spring-boot-starter-validation"),
                    databaseType,
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
            Agentic Code Generator
            
            Usage:
              agentic <brd-file> [options]
            
            Required:
              <brd-file>                 Path to BRD input file
            
            Options:
              -a <groupId>               Set groupId (default: com.example)
              -v <version>               Set project version (default: 0.0.1-SNAPSHOT)
              --boot <version>           Spring Boot version (default: 3.2.2)
              --db <type>                Database: mysql | postgres | h2 | none
              --swagger off              Disable Swagger
              -o, --out <dir>            Output directory
              -t <target>                Target type (code | erd)
              --dry-run                  Generate without writing files
              -h, --help                 Show this help message
            
            Examples:
              agentic brd.txt
              agentic brd.txt --db h2
              agentic brd.txt -a com.gosi --db mysql
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
    private static String readBrdFile(Path path) throws Exception {

        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".txt")) {
            return Files.readString(path).replace("\r\n", "\n");
        }

        if (fileName.endsWith(".pdf")) {
            try (org.apache.pdfbox.pdmodel.PDDocument document =
                         org.apache.pdfbox.pdmodel.PDDocument.load(path.toFile())) {

                org.apache.pdfbox.text.PDFTextStripper stripper =
                        new org.apache.pdfbox.text.PDFTextStripper();

                return stripper.getText(document);
            }
        }

        if (fileName.endsWith(".docx")) {
            try (java.io.InputStream is = Files.newInputStream(path);
                 org.apache.poi.xwpf.usermodel.XWPFDocument doc =
                         new org.apache.poi.xwpf.usermodel.XWPFDocument(is)) {

                org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor =
                        new org.apache.poi.xwpf.extractor.XWPFWordExtractor(doc);

                return extractor.getText();
            }
        }

        if (fileName.endsWith(".doc")) {
            try (java.io.InputStream is = Files.newInputStream(path);
                 org.apache.poi.hwpf.HWPFDocument doc =
                         new org.apache.poi.hwpf.HWPFDocument(is)) {

                org.apache.poi.hwpf.extractor.WordExtractor extractor =
                        new org.apache.poi.hwpf.extractor.WordExtractor(doc);

                return extractor.getText();
            }
        }

        throw new IllegalArgumentException(
                "Unsupported BRD format. Supported: .txt, .pdf, .doc, .docx"
        );
    }

}
