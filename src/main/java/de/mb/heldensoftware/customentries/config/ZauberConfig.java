package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;

public class ZauberConfig {
    @JsonProperty(required = true)
    String name;
    @JsonProperty(required = true)
    String kategorie;
    @JsonProperty(required = true)
    ArrayList<String> merkmale;
    @JsonProperty(required = true)
    String probe;
    @JsonProperty
    String mod = "";
    @JsonProperty
    String quelle = "";
    @JsonProperty
    ArrayList<String> settings = new ArrayList<>(); // TODO default
    @JsonProperty
    HashMap<String, Integer> verbreitung = new HashMap<>();
    @JsonProperty
    ArrayList<String> spezialisierungen = new ArrayList<>();
}
