package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class RepraesentationConfig {
    @JsonProperty(required = true)
    String name;
    @JsonProperty(required = true)
    String abkürzung;

    @JsonProperty
    boolean ritualkenntnis = false;
    @JsonProperty
    HashMap<String, Integer> zauber = new HashMap<>();  // Name => Verbreitung
}
