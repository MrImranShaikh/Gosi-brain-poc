package ai.agentic.llm;

public class DummyLLMClient implements LLMClient {

    @Override
    public String generate(String prompt) {
        return """
        {
          "modules": [
            {
              "name": "employee",
              "entities": ["Employee"]
            }
          ]
        }
        """;
    }
}
