package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import java.util.ArrayList;
import java.util.HashMap;

@JsonClassDescription("Eigener, neuer Zauber")
public class ZauberConfig {
    @JsonProperty(required = true)
    @JsonPropertyDescription("Zaubername, darf noch nicht existieren. Beispiel: \"Inarcanitas\"")
    String name;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Steigerungs-Kategorie (\"A\"-\"H\")")
    @JsonSchema(defaultValue="A", pattern = "^[A-H]$")
    String kategorie;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Merkmale, Kurzfassung und Langfassung möglich. Beispiel: [\"Anti\", \"Kraft\", \"Metamagie\"]")
    ArrayList<String> merkmale;

    @JsonProperty(required = true)
    @JsonPropertyDescription("Probe \"XX/YY/ZZ\", auch \"**\" möglich. Beispiel: \"MU/KL/KL\"")
    @JsonSchema(defaultValue="XX/YY/ZZ", pattern = "^[A-Z*]{2}/[A-Z*]{2}/[A-Z*]{2}$")
    String probe;

    @JsonProperty
    @JsonPropertyDescription("Proben-Modifikator wie \"+MR\", \"+Mod\", ...")
    String mod = "";

    @JsonProperty
    @JsonPropertyDescription("Quellenangabe im Format \"XXX:<seitenzahl>\". Beispiel: \"LCD:123\"")
    String quelle = "";

    @JsonProperty
    @JsonPropertyDescription("Spielwelten, in denen der Zauber zur Verfügung steht. Beispiel: [\"Aventurien\"]")
    @JsonSchema(defaultValue="[Aventurien]")
    ArrayList<String> settings = new ArrayList<>(); // TODO default

    @JsonProperty
    @JsonPropertyDescription("Verbreitung in Repräsentationen. Beispiele: \n" +
            "{\"Mag\": 7}\n" +
            "{\"Hex(Mag)\": 2}")
    HashMap<String, Integer> verbreitung = new HashMap<>();

    @JsonProperty
    @JsonPropertyDescription("Mögliche Spezialisierungen. Beispiel: [\"Reichweite\", \"Zauberdauer\", \"Variante X\"]")
    ArrayList<String> spezialisierungen = new ArrayList<>();
}
