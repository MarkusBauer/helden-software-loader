package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Config {
    @JsonProperty
    ArrayList<ZauberConfig> zauber = new ArrayList<>();
    @JsonProperty
    ArrayList<SonderfertigkeitConfig> sonderfertigkeiten = new ArrayList<>();
    @JsonProperty
    ArrayList<RepraesentationConfig> repräsentationen = new ArrayList<>();
}
