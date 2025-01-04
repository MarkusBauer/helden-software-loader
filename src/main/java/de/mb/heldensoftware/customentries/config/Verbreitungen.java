package de.mb.heldensoftware.customentries.config;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class Verbreitungen extends HashMap<@NotBlank String, @NotNull @Min(1) @Max(7) Integer> {

    // Manually validate superclass constraints
    @Valid
    private Map<@NotBlank String, @NotNull @Min(1) @Max(7) Integer> getAsMap() {
        return this;
    }
}
