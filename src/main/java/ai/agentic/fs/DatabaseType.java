package ai.agentic.fs;

public enum DatabaseType {

    NONE,
    MYSQL,
    POSTGRES,
    H2;

    public static DatabaseType from(String value) {

        if (value == null || value.isBlank()) {
            return NONE;
        }

        return switch (value.trim().toLowerCase()) {
            case "mysql" -> MYSQL;
            case "postgres", "postgresql" -> POSTGRES;
            case "h2" -> H2;
            case "none" -> NONE;
            default -> throw new IllegalArgumentException(
                    "Unsupported database type: " + value +
                            ". Allowed values: mysql | postgres | h2 | none"
            );
        };
    }
}
