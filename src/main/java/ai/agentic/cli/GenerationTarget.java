package ai.agentic.cli;

public enum GenerationTarget {
    ERD,
    ENTITY,
    REPOSITORY,
    SERVICE,
    CONTROLLER,
    CODE; // full pipeline

    public static GenerationTarget from(String value) {
        if (value == null) {
            return CODE;
        }

        return switch (value.toLowerCase()) {
            case "erd" -> ERD;
            case "entity", "entities" -> ENTITY;
            case "repo", "repository", "repositories" -> REPOSITORY;
            case "service", "services" -> SERVICE;
            case "controller", "controllers" -> CONTROLLER;
            case "code" -> CODE;
            default -> throw new IllegalArgumentException(
                    "Unknown target: " + value +
                            " (erd | entity | repository | service | controller | code)"
            );
        };
    }
}
