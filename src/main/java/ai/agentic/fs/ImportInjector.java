package ai.agentic.fs;

public class ImportInjector {

    public static String injectImports(String code) {

        if (code == null) return "";

        String cleaned = sanitize(code);

        StringBuilder imports = new StringBuilder();

        if (cleaned.contains("List<")) {
            imports.append("import java.util.List;\n");
        }

        if (cleaned.contains("@RestController") ||
                cleaned.contains("@GetMapping") ||
                cleaned.contains("@PostMapping") ||
                cleaned.contains("@PutMapping") ||
                cleaned.contains("@DeleteMapping")) {
            imports.append("import org.springframework.web.bind.annotation.*;\n");
        }

        if (cleaned.contains("@Service")) {
            imports.append("import org.springframework.stereotype.Service;\n");
        }

        if (cleaned.contains("@Repository")) {
            imports.append("import org.springframework.stereotype.Repository;\n");
        }

        if (cleaned.contains("@Entity") ||
                cleaned.contains("@Id") ||
                cleaned.contains("@Column")) {
            imports.append("import jakarta.persistence.*;\n");
        }

        if (cleaned.contains("@Operation") ||
                cleaned.contains("@Tag")) {
            imports.append("import io.swagger.v3.oas.annotations.*;\n");
        }

        return mergeImports(cleaned, imports.toString());
    }

    private static String mergeImports(String code, String importBlock) {

        int packageIndex = code.indexOf("package ");
        if (packageIndex == -1) {
            return importBlock + "\n" + code;
        }

        int semicolonIndex = code.indexOf(";", packageIndex);
        if (semicolonIndex == -1) {
            return code;
        }

        return code.substring(0, semicolonIndex + 1)
                + "\n\n"
                + importBlock
                + "\n"
                + code.substring(semicolonIndex + 1);
    }

    private static String sanitize(String code) {

        String cleaned = code.trim();

        cleaned = cleaned.replace("```java", "");
        cleaned = cleaned.replace("```", "");

        int packageIndex = cleaned.indexOf("package ");
        if (packageIndex > 0) {
            cleaned = cleaned.substring(packageIndex);
        }

        return cleaned.trim();
    }
}
