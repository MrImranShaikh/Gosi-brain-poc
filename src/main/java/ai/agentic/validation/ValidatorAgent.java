package ai.agentic.validation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ValidatorAgent {

    public ValidationResult validate(Path projectRoot) {

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(projectRoot.toFile());
            builder.command("cmd", "/c", "mvnw.cmd", "clean", "compile");

            builder.redirectErrorStream(true);

            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            return new ValidationResult(exitCode == 0, output.toString());

        } catch (Exception e) {
            return new ValidationResult(false, e.getMessage());
        }
    }
}
