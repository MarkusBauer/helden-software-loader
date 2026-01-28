package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@JsonClassDescription("Eigener, neuer myranischer Zauber")
public class MyranorZauberConfig {
    @JsonProperty(required = true)
    @JsonPropertyDescription("Zaubername, darf noch nicht existieren. Beispiel: \"Beschwörung von Feuergeistern\"")
    @NotBlank
    public String name;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Steigerungs-Kategorie (\"A\"-\"H\")")
    @JsonSchema(defaultValue = "A", pattern = "^[A-H]$")
    @NotBlank
    public String kategorie;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Merkmal/Quelle, Kurzfassung und Langfassung möglich. Beispiele: \"Elementar (Feuer)\", \"Freiheit\"")
    @NotBlank
    public String merkmal;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Art des Zaubers. \"Wesen\" oder \"Essenz\".")
    @JsonSchema(defaultValue = "Wesen")
    @NotBlank
    public String art;

    @JsonProperty
    @JsonPropertyDescription("Proben-Modifikator wie \"+MR\", \"+Mod\", ...")
    @NotNull
    public String mod = "";

    @JsonProperty
    @JsonPropertyDescription("Quellenangabe im Format \"XXX:<seitenzahl>\". Beispiel: \"LCD:123\"")
    @NotNull
    public String quelle = "";

    @JsonProperty
    @JsonPropertyDescription("Spielwelten, in denen der Zauber zur Verfügung steht. Beispiel: [\"Myranor\"]")
    @JsonSchema(defaultValue = "[Myranor]")
    @NotNull
    public ArrayList<@NotBlank String> settings = new ArrayList<>(Collections.singletonList("Myranor"));

    @JsonProperty
    @JsonPropertyDescription("Mögliche Spezialisierungen, zusätzlich zu den standardmäßigen Spezialisierungen. Beispiel: [\"Variante X\"]")
    @NotNull
    public ArrayList<@NotBlank String> spezialisierungen = new ArrayList<>();

    @Override
    public String toString() {
        return "MyranorZauberConfig{" +
                "name='" + name + '\'' +
                ", kategorie='" + kategorie + '\'' +
                ", merkmal=" + merkmal +
                ", art='" + art + '\'' +
                ", mod='" + mod + '\'' +
                ", quelle='" + quelle + '\'' +
                ", settings=" + settings +
                ", spezialisierungen=" + spezialisierungen +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MyranorZauberConfig that = (MyranorZauberConfig) o;
        return Objects.equals(name, that.name) && Objects.equals(kategorie, that.kategorie) && Objects.equals(merkmal, that.merkmal) && Objects.equals(art, that.art) && Objects.equals(mod, that.mod) && Objects.equals(quelle, that.quelle) && Objects.equals(settings, that.settings) && Objects.equals(spezialisierungen, that.spezialisierungen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kategorie, merkmal, art, mod, quelle, settings, spezialisierungen);
    }
}
