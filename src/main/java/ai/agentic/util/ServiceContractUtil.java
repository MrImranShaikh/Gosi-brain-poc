package ai.agentic.util;

public class ServiceContractUtil {

    public static String extractMethodSignatures(String interfaceCode) {

        if (interfaceCode == null || interfaceCode.isBlank()) {
            return "";
        }

        StringBuilder methods = new StringBuilder();

        String[] lines = interfaceCode.split("\n");

        for (String line : lines) {

            String trimmed = line.trim();

            if (trimmed.startsWith("import")
                    || trimmed.startsWith("@")
                    || trimmed.startsWith("public interface")
                    || trimmed.startsWith("}")
                    || trimmed.isBlank()) {
                continue;
            }

            // Capture interface method signatures
            if (trimmed.endsWith(";") && trimmed.contains("(")) {

                // Remove trailing semicolon
                String signature = trimmed.replace(";", "").trim();

                methods.append(signature).append(";\n");
            }
        }

        return methods.toString();
    }

}
