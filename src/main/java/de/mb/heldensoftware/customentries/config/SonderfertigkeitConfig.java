package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class SonderfertigkeitConfig {
    @JsonProperty(required = true)
    String name;
    @JsonProperty(required = true)
    int kosten;
    @JsonProperty
    String kategorie = "Allgemein";
    @JsonProperty
    ArrayList<SFVariante> varianten = new ArrayList<>();
    @JsonProperty
    ArrayList<BedingungConfig> bedingungen = new ArrayList<>();

    @JsonProperty
    ArrayList<String> liturgien;  // für neue Liturgiekenntnis
    @JsonProperty
    ArrayList<String> liturgiekenntnis;  // für neue Liturgien
    @JsonProperty
    int grad = 0;

    public static class SFVariante {
        @JsonProperty(required = true)
        String name;
        @JsonProperty
        Integer kosten = null;

        public SFVariante() {
        }

        public SFVariante(String name) {
            this.name = name;
        }
    }
}
