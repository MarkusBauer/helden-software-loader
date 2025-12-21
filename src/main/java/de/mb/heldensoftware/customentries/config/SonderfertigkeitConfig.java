package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Objects;

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
    public ArrayList<@NotNull @Valid SFVariante> varianten = new ArrayList<>();

    @JsonProperty
    @JsonPropertyDescription("Bedingung, damit diese Sonderfertigkeit aktiviert werden kann. Siehe Manual.")
    @NotNull
    public ArrayList<@Valid BedingungConfig> bedingungen = new ArrayList<>();

    @JsonProperty
    @JsonPropertyDescription("(für neue Liturgie-Kenntnis): Liste mit möglichen Liturgien")
    @NotNull
    public ArrayList<@NotBlank String> liturgien = new ArrayList<>();  // für neue Liturgiekenntnis

    @JsonProperty
    @JsonPropertyDescription("(für neue Liturgie): mögliche Liturgiekenntnis. Beispiel: \"Praios\"")
    @NotNull
    public ArrayList<@NotBlank String> liturgiekenntnis = new ArrayList<>();  // für neue Liturgien

    @JsonProperty
    @JsonPropertyDescription("(für neue Liturgie): Grad (1-7)")
    @Min(0)
    @Max(7)
    public int grad = 0;

    public static class SFVariante {
        @JsonProperty(required = true)
        @JsonPropertyDescription("Varianten-Name")
        @NotBlank
        public String name;

        @JsonProperty
        @JsonPropertyDescription("(optional) Kosten dieser Variante (falls unterschiedlich)")
        @PositiveOrZero
        public Integer kosten = null;

        public SFVariante() {
        }

        public SFVariante(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            SFVariante that = (SFVariante) o;
            return Objects.equals(name, that.name) && Objects.equals(kosten, that.kosten);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, kosten);
        }
    }

    public void autoFixNames() {
        for (int i = 0; i < liturgiekenntnis.size(); i++) {
            String s = liturgiekenntnis.get(i);
            if (!s.startsWith("Liturgiekenntnis ("))
                liturgiekenntnis.set(i, "Liturgiekenntnis (" + s + ")");
        }
        for (int i = 0; i < liturgien.size(); i++) {
            String s = liturgien.get(i);
            if (!s.startsWith("Liturgie: "))
                liturgien.set(i, "Liturgie: " + s);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SonderfertigkeitConfig that = (SonderfertigkeitConfig) o;
        return kosten == that.kosten && grad == that.grad && Objects.equals(name, that.name) && Objects.equals(kategorie, that.kategorie) && Objects.equals(varianten, that.varianten) && Objects.equals(bedingungen, that.bedingungen) && Objects.equals(liturgien, that.liturgien) && Objects.equals(liturgiekenntnis, that.liturgiekenntnis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kosten, kategorie, varianten, bedingungen, liturgien, liturgiekenntnis, grad);
    }

    public boolean isFreitextVariante() {
        return varianten.size() == 1 && (varianten.get(0).name.isEmpty() || varianten.get(0).name.equalsIgnoreCase("freitext"));
    }
}
