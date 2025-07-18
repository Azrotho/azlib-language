package fr.azrotho.azlib_language;

import com.fasterxml.jackson.databind.JsonNode;

public class Language {
    private String id;
    private JsonNode node;

    public Language(String id, JsonNode node) {
        this.id = id;
        this.node = node;
    }

    public String id() {
        return id;
    }

    public JsonNode node() {
        return node;
    }
}
