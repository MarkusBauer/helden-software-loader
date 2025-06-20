package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

public class MerkmalConfig {
    @JsonProperty(required = true)
    @JsonPropertyDescription("Vollständiger Name des Merkmals. Beispiel: \"Erweckung\"")
    @NotBlank
    public String name;

    @JsonProperty(value = "abkürzung", required = true)
    @JsonPropertyDescription("Abkürzung. Beispiel: \"Erw\"")
    @Size(min = 1, max = 8)
    public String abkuerzung;

    @JsonProperty(value = "stufe")
    @JsonPropertyDescription("Stufe (1-3). 1 = 100AP, 2 = 200AP, 3 = 300AP")
    @JsonSchema(defaultValue = "2")
    @Min(1)
    @Max(3)
    public int stufe = 2;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MerkmalConfig that = (MerkmalConfig) o;
        return stufe == that.stufe && Objects.equals(name, that.name) && Objects.equals(abkuerzung, that.abkuerzung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, abkuerzung, stufe);
    }
}
