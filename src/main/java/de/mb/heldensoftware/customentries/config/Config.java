package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Objects;

@JsonIgnoreProperties("$schema")
public class Config {
    @JsonProperty
    @Valid
    public ArrayList<ZauberConfig> zauber = new ArrayList<>();

    @JsonProperty
    @Valid
    public ArrayList<SonderfertigkeitConfig> sonderfertigkeiten = new ArrayList<>();

    @JsonProperty("repräsentationen")
    @Valid
    public ArrayList<RepraesentationConfig> repraesentationen = new ArrayList<>();

    // Filename or whatever where we got that config from.
    // Optional, not compared etc.
    public String source = null;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(zauber, config.zauber) && Objects.equals(sonderfertigkeiten, config.sonderfertigkeiten) && Objects.equals(repraesentationen, config.repraesentationen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zauber, sonderfertigkeiten, repraesentationen);
    }
}
