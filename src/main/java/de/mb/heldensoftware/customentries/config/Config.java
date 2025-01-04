package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.ArrayList;

public class Config {
    @JsonProperty
    @Valid
    ArrayList<ZauberConfig> zauber = new ArrayList<>();

    @JsonProperty
    @Valid
    ArrayList<SonderfertigkeitConfig> sonderfertigkeiten = new ArrayList<>();

    @JsonProperty("repräsentationen")
    @Valid
    ArrayList<RepraesentationConfig> repraesentationen = new ArrayList<>();
}
