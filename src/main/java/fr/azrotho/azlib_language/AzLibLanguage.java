package fr.azrotho.azlib_language;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AzLibLanguage {
    private Map<String, Language> languages;
    String defaultLanguageID;
    File defaultLanguageFile;

    public AzLibLanguage(String defaultLanguageID, File defaultLanguageFile) {
        this.languages = new HashMap<>();
        this.defaultLanguageID = defaultLanguageID;
        this.defaultLanguageFile = defaultLanguageFile;
    }

    public void init() {
        register(this.defaultLanguageFile);
    }

    public void register(File file) {
        if(!file.exists()) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        }
        if(file.isDirectory()) {
            throw new RuntimeException("File is a directory: " + file.getAbsolutePath());
        }
        if(!file.canRead()) {
            throw new RuntimeException("File cannot be read: " + file.getAbsolutePath());
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(file);

            if(!exists(node, "/language/id")) {
                throw new Exception("Language ID not found in file: " + file.getAbsolutePath());
            }
            JsonNode idNode = node.at("/language/id");
            this.languages.put(idNode.asText(), new Language(idNode.asText(), node));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // UNSAFE: This method should be used with caution, please use it only if you know what you are doing.
    public List<Language> getLanguages() {
        return new ArrayList<>(this.languages.values());
    }

    // UNSAFE: This method should be used with caution, please use it only if you know what you are doing.
    public Language getLanguage(String id) {
        return this.languages.get(id);
    }

    public String getText(String langID, String langPath) {
        // This method will return the text at the specified path in the specified language, or a string from the default language if the specified language or path does not exist.
        Language language = this.languages.get(langID);
        if (language == null) {
            language = this.languages.get(this.defaultLanguageID);
            if (language == null) {
               throw new RuntimeException("Language ID not found AND Default language ID not found: " + langID + ", " + this.defaultLanguageID);
            }
        }
        JsonNode node = language.node().at(langPath);
        if (node.isMissingNode()) {
            // Fallback to default language
            Language defaultLanguage = this.languages.get(this.defaultLanguageID);
            if (defaultLanguage == null) {
                throw new RuntimeException("Default language ID not found: " + this.defaultLanguageID);
            }
            node = defaultLanguage.node().at(langPath);
            if (node.isMissingNode()) {
                throw new RuntimeException("Path not found in default language: " + langPath);
            }
        }
        return node.asText();
    }

    public String getText(String langID, String langPath, String fallback) {
        // This method will return the text at the specified path in the specified language, or a fallback string if the specified language or path does not exist.
        Language language = this.languages.get(langID);
        if (language == null) {
            language = this.languages.get(this.defaultLanguageID);
            if (language == null) {
                return fallback;
            }
        }
        JsonNode node = language.node().at(langPath);
        if (node.isMissingNode()) {
            return fallback; // Return fallback string
        }
        return node.asText();
    }

    public String getTextWithoutException(String langID, String langPath) {
        Language language = this.languages.get(langID);
        if (language == null) {
            language = this.languages.get(this.defaultLanguageID);
            if (language == null) {
                return langID + " (Default language ID not found: " + this.defaultLanguageID + ")";
            }
        }
        JsonNode node = language.node().at(langPath);
        if (node.isMissingNode()) {
            // Fallback to default language
            Language defaultLanguage = this.languages.get(this.defaultLanguageID);
            if (defaultLanguage == null) {
                return "Path Missing :" + langPath + " (Default language ID not found: " + this.defaultLanguageID + ")";
            }
            node = defaultLanguage.node().at(langPath);
            if (node.isMissingNode()) {
                return "Path Missing :" + langPath + " (Also not found in: " + this.defaultLanguageID + ")";
            }
        }
        return node.asText();
    }

    public static boolean exists(JsonNode root, String pointer) {
        return !root.at(pointer).isMissingNode();
    }
}