package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.imifou.jsonschema.module.addon.annotation.JsonSchema;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Objects;

public class BedingungConfig {
    @JsonProperty(required = true)
    @JsonPropertyDescription("Bedingungs-Art. Beispiel: \"Sonderfertigkeit\"")
    @JsonSchema(defaultValue = "Talent")
    @NotNull
    public BedingungType type;

    @JsonProperty
    @JsonPropertyDescription("Für Typ Sonderfertigkeit, Talent, Zauber, Eigenschaft: was vorausgesetzt wird")
    @JsonSchema(defaultValue = "Magiekunde")
    public String name = null;

    @JsonProperty
    @JsonPropertyDescription("Mindest-Wert der Voraussetzung \"name\"")
    @JsonSchema(defaultValue = "0")
    public int value = 0;

    @JsonProperty
    @JsonPropertyDescription("Für Typ or, and: Liste mit weiteren Bedingungen")
    public ArrayList<@Valid BedingungConfig> bedingungen = null;

    @JsonProperty
    @JsonPropertyDescription("Invertiere diese Bedingung (Bedingung darf NICHT erfüllt sein)")
    @JsonSchema(defaultValue = "false")
    public boolean not = false;


    @AssertTrue(message = "ist erforderlich")
    private boolean isName() {
        if (type != BedingungType.or && type != BedingungType.and && type != BedingungType.LKW && type != BedingungType.Leiteigenschaft) {
            return name != null && !name.isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "ist erforderlich")
    private boolean isBedingungen() {
        if (type == BedingungType.or || type == BedingungType.and) {
            return bedingungen != null && !bedingungen.isEmpty();
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BedingungConfig that = (BedingungConfig) o;
        return value == that.value && not == that.not && type == that.type && Objects.equals(name, that.name) && Objects.equals(bedingungen, that.bedingungen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value, bedingungen, not);
    }
}
