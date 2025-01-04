package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;

public class SonderfertigkeitConfig {
    @JsonProperty(required = true)
    @JsonPropertyDescription("Name der Sonderfertigkeit, darf noch nicht existieren")
    @NotBlank
    public String name;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Abenteuerpunkte-Kosten der Sonderfertigkeit")
    @JsonSchema(defaultValue = "0")
    @Min(0)
    public int kosten = 0;

    @JsonProperty
    @JsonPropertyDescription("Kategorie der Sonderfertigkeit. Beispiel: \"Geländekunde\", \"Magisch\", ...")
    @JsonSchema(defaultValue = "Allgemein")
    @NotBlank
    public String kategorie = "Allgemein";

    @JsonProperty
    @JsonPropertyDescription("(optional) mögliche Varianten, beispielsweise \"Bogen\" und \"Armbrust\" bei Scharfschütze. \n" +
            "Format: - \"Varianten-Name\"  oder\n" +
            "        - name: \"Varianten-Name\"" +
            "          kosten: 250  # Abenteuerpunkte pro Variante")
    @NotNull
    ArrayList<@NotNull @Valid SFVariante> varianten = new ArrayList<>();

    @JsonProperty
    @JsonPropertyDescription("Bedingung, damit diese Sonderfertigkeit aktiviert werden kann. Siehe Manual.")
    @NotNull
    ArrayList<@Valid BedingungConfig> bedingungen = new ArrayList<>();

    @JsonProperty
    @JsonPropertyDescription("(für neue Liturgie-Kenntnis): Liste mit möglichen Liturgien")
    ArrayList<@NotBlank String> liturgien;  // für neue Liturgiekenntnis

    @JsonProperty
    @JsonPropertyDescription("(für neue Liturgie): mögliche Liturgiekenntnis. Beispiel: \"Praios\"")
    ArrayList<@NotBlank String> liturgiekenntnis;  // für neue Liturgien

    @JsonProperty
    @JsonPropertyDescription("(für neue Liturgie): Grad (1-7)")
    @Min(0)
    @Max(7)
    int grad = 0;

    public static class SFVariante {
        @JsonProperty(required = true)
        @JsonPropertyDescription("Varianten-Name")
        @NotBlank
        String name;

        @JsonProperty
        @JsonPropertyDescription("(optional) Kosten dieser Variante (falls unterschiedlich)")
        @PositiveOrZero
        Integer kosten = null;

        public SFVariante() {
        }

        public SFVariante(String name) {
            this.name = name;
        }
    }
}
