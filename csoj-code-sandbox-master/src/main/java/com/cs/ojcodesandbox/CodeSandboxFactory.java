package com.cs.ojcodesandbox;

import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.strategy.JavaLanguageStrategy;
import com.cs.ojcodesandbox.strategy.PythonLanguageStrategy;
import com.cs.ojcodesandbox.strategy.CppLanguageStrategy;
import com.cs.ojcodesandbox.strategy.CSharpLanguageStrategy;
import com.cs.ojcodesandbox.strategy.JavaScriptLanguageStrategy;
import com.cs.ojcodesandbox.strategy.GoLanguageStrategy;
import com.cs.ojcodesandbox.strategy.RubyLanguageStrategy;
import com.cs.ojcodesandbox.strategy.SwiftLanguageStrategy;
import com.cs.ojcodesandbox.strategy.CLanguageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

@Component
public class CodeSandboxFactory {

    private final Map<String, CodeSandboxStrategy> strategyMap = new HashMap<>();

    @Autowired(required = false)
    private JavaLanguageStrategy javaLanguageStrategy;

    @Autowired(required = false)
    private PythonLanguageStrategy pythonLanguageStrategy;

    @Autowired(required = false)
    private CppLanguageStrategy cppLanguageStrategy;

    @Autowired(required = false)
    private CSharpLanguageStrategy cSharpLanguageStrategy;

    @Autowired(required = false)
    private JavaScriptLanguageStrategy javaScriptLanguageStrategy;

    @Autowired(required = false)
    private GoLanguageStrategy goLanguageStrategy;

    @Autowired(required = false)
    private RubyLanguageStrategy rubyLanguageStrategy;

    @Autowired(required = false)
    private SwiftLanguageStrategy swiftLanguageStrategy;

    @Autowired(required = false)
    private CLanguageStrategy cLanguageStrategy;

    @PostConstruct
    public void init() {
        if (javaLanguageStrategy != null) {
            strategyMap.put("java", javaLanguageStrategy);
        }
        if (pythonLanguageStrategy != null) {
            strategyMap.put("python", pythonLanguageStrategy);
        }
        if (cppLanguageStrategy != null) {
            strategyMap.put("cpp", cppLanguageStrategy);
        }
        if (cSharpLanguageStrategy != null) {
            strategyMap.put("csharp", cSharpLanguageStrategy);
        }
        if (javaScriptLanguageStrategy != null) {
            strategyMap.put("javascript", javaScriptLanguageStrategy);
        }
        if (goLanguageStrategy != null) {
            strategyMap.put("go", goLanguageStrategy);
        }
        if (rubyLanguageStrategy != null) {
            strategyMap.put("ruby", rubyLanguageStrategy);
        }
        if (swiftLanguageStrategy != null) {
            strategyMap.put("swift", swiftLanguageStrategy);
        }
        if (cLanguageStrategy != null) {
            strategyMap.put("c", cLanguageStrategy);
        }
        // Add other languages to the map
    }

    public CodeSandboxStrategy getStrategy(String language) {
        CodeSandboxStrategy strategy = strategyMap.get(language.toLowerCase());
        if (strategy == null) {
            // Check if docker is available, if not, explain that the strategy is unavailable
            String dockerAvailable = System.getProperty("docker.available");
            if (!"true".equals(dockerAvailable)) {
                throw new IllegalArgumentException("Unsupported language or Docker environment not available: " + language);
            }
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return strategy;
    }
}