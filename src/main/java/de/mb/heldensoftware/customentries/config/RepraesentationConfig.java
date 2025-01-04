package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

public class RepraesentationConfig {
    @JsonProperty(required = true)
    @JsonPropertyDescription("Name der eigenen Repräsentation. Beispiel: \"Hochelfisch\"")
    @NotBlank
    public String name;

    @JsonProperty(value = "abkürzung", required = true)
    @JsonPropertyDescription("Abkürzung. Beispiel: \"Hoc\"")
    @JsonSchema(defaultValue = "Xxx")
    @Size(min = 1, max = 5)
    public String abkuerzung;

    @JsonProperty
    @JsonPropertyDescription("true um eine passende Ritualkenntnis anzulegen")
    public boolean ritualkenntnis = false;

    @JsonProperty
    @JsonPropertyDescription("Verbreitungen der einzelnen Zauber in dieser Repräsentation. Nicht angegebene Zauber können später nur per Editor aktiviert werden. Beispiel:\n" +
            "{\"Armatrutz\": 7}")
    @NotNull
    @Valid
    public Verbreitungen zauber = new Verbreitungen();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RepraesentationConfig that = (RepraesentationConfig) o;
        return ritualkenntnis == that.ritualkenntnis && Objects.equals(name, that.name) && Objects.equals(abkuerzung, that.abkuerzung) && Objects.equals(zauber, that.zauber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, abkuerzung, ritualkenntnis, zauber);
    }
}
