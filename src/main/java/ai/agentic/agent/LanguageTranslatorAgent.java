package ai.agentic.agent;

import ai.agentic.llm.LLMClient;

public class LanguageTranslatorAgent {
    public static String translatePreservingFormat(
            LLMClient llm,
            String content,
            String language
    ) {

        String prompt = """
    You are a professional document translator.

    Translate the following document into %s.

    STRICT RULES:
    - Preserve all formatting exactly.
    - Do NOT modify structure.
    - Do NOT remove or add headings.
    - Do NOT modify code blocks.
    - Do NOT change indentation.
    - Do NOT change JSON, XML, or markdown syntax.
    - Only translate natural language text.

    Return ONLY the translated content.
    Do not add explanations.

    DOCUMENT:
    %s
    """.formatted(language, content);

        return llm.generate(prompt);
    }

}
