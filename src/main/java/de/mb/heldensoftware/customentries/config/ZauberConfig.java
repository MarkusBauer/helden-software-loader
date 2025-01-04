package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@JsonClassDescription("Eigener, neuer Zauber")
public class ZauberConfig {
    @JsonProperty(required = true)
    @JsonPropertyDescription("Zaubername, darf noch nicht existieren. Beispiel: \"Inarcanitas\"")
    @NotBlank
    public String name;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Steigerungs-Kategorie (\"A\"-\"H\")")
    @JsonSchema(defaultValue = "A", pattern = "^[A-H]$")
    @NotBlank
    public String kategorie;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Merkmale, Kurzfassung und Langfassung möglich. Beispiel: [\"Anti\", \"Kraft\", \"Metamagie\"]")
    @NotEmpty
    public ArrayList<@NotBlank String> merkmale;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Probe \"XX/YY/ZZ\", auch \"**\" möglich. Beispiel: \"MU/KL/KL\"")
    @JsonSchema(defaultValue = "XX/YY/ZZ")
    @NotBlank
    @Pattern(regexp = "^[A-Z*]{2}/[A-Z*]{2}/[A-Z*]{2}$", message = "muss die Form MU/KL/IN haben")
    public String probe;

    @JsonProperty
    @JsonPropertyDescription("Proben-Modifikator wie \"+MR\", \"+Mod\", ...")
    @NotNull
    public String mod = "";

    @JsonProperty
    @JsonPropertyDescription("Quellenangabe im Format \"XXX:<seitenzahl>\". Beispiel: \"LCD:123\"")
    @NotNull
    public String quelle = "";

    @JsonProperty
    @JsonPropertyDescription("Spielwelten, in denen der Zauber zur Verfügung steht. Beispiel: [\"Aventurien\"]")
    @JsonSchema(defaultValue = "[Alle]")
    @NotNull
    public ArrayList<@NotBlank String> settings = new ArrayList<>(); // TODO default

    @JsonProperty
    @JsonPropertyDescription("Verbreitung in Repräsentationen. Beispiele: \n" +
            "{\"Mag\": 7}\n" +
            "{\"Hex(Mag)\": 2}")
    @NotNull
    @Valid
    public Verbreitungen verbreitung = new Verbreitungen();
    // public HashMap<@NotBlank String, @Min(1) @Max(7) Integer> verbreitung = new HashMap<>();

    @JsonProperty
    @JsonPropertyDescription("Mögliche Spezialisierungen. Beispiel: [\"Reichweite\", \"Zauberdauer\", \"Variante X\"]")
    @NotNull
    public ArrayList<@NotBlank String> spezialisierungen = new ArrayList<>();

    @Override
    public String toString() {
        return "ZauberConfig{" +
                "name='" + name + '\'' +
                ", kategorie='" + kategorie + '\'' +
                ", merkmale=" + merkmale +
                ", probe='" + probe + '\'' +
                ", mod='" + mod + '\'' +
                ", quelle='" + quelle + '\'' +
                ", settings=" + settings +
                ", verbreitung=" + verbreitung +
                ", spezialisierungen=" + spezialisierungen +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ZauberConfig that = (ZauberConfig) o;
        return Objects.equals(name, that.name) && Objects.equals(kategorie, that.kategorie) && Objects.equals(merkmale, that.merkmale) && Objects.equals(probe, that.probe) && Objects.equals(mod, that.mod) && Objects.equals(quelle, that.quelle) && Objects.equals(settings, that.settings) && Objects.equals(verbreitung, that.verbreitung) && Objects.equals(spezialisierungen, that.spezialisierungen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kategorie, merkmale, probe, mod, quelle, settings, verbreitung, spezialisierungen);
    }
}
