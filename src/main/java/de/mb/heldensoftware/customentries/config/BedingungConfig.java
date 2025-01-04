package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class BedingungConfig {
    @JsonProperty(required = true)
    String type;
    @JsonProperty
    String name = null;
    @JsonProperty
    int value = 0;
    @JsonProperty
    ArrayList<BedingungConfig> bedingungen = null;
    @JsonProperty
    boolean not = false;
}
