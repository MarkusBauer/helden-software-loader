package de.mb.heldensoftware.customentries;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.imifou.jsonschema.module.addon.AddonModule;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationModule;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationOption;
import de.mb.heldensoftware.customentries.config.Config;
import de.mb.heldensoftware.customentries.config.Loader;
import org.junit.Test;

import java.io.*;

public class ConfigTest {
    @Test
    public void testExampleJsonFile() {
        try (Reader r = Loader.preprocessStream(getClass().getResourceAsStream("/examples/examples.json"))) {
            Loader.loadFromJson(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testExampleYamlFile() {
        try (Reader r = Loader.preprocessStream(getClass().getResourceAsStream("/examples/examples.yaml"))) {
            Loader.loadFromYaml(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testZauberValidation() {
        String[] invalidJsons = new String[]{
                "{\"name\": \"Test\"}",
                "{\"name\": \"Test\", \"kategorie\": \"F\", \"probe\": \"MU/KL/KL\", \"merkmale\": [\"\"]}",
                "{\"name\": \"Test\", \"kategorie\": \"F\", \"probe\": \"MU/KL/KL/XX\", \"merkmale\": [\"a\"]}",
                "{\"name\": \"Test\", \"kategorie\": \"F\", \"probe\": \"MU/KL/KL\", \"merkmale\": [\"a\"], \"verbreitung\": {\"\": 1}}",
                "{\"name\": \"Test\", \"kategorie\": \"F\", \"probe\": \"MU/KL/KL\", \"merkmale\": [\"a\"], \"verbreitung\": {\"Mag\": 8}}",
        };
        for (String json : invalidJsons) {
            assertInvalidJson("{\"zauber\": [" + json + "]}");
        }
    }

    @Test
    public void testRepraesentationValidation() {
        String[] invalidJsons = new String[]{
                "{\"name\": \"Test\", \"abkürzung\": \"abcdef\"}",
                "{\"name\": \"Test\", \"abkürzung\": \"Tes\", \"zauber\": {\"\": 1}}",
        };
        for (String json : invalidJsons) {
            assertInvalidJson("{\"repräsentationen\": [" + json + "]}");
        }
    }

    @Test
    public void testSonderfertigkeitValidation() {
        String[] validJsons = new String[]{
                "{\"name\": \"Test\", \"kosten\": 1, \"varianten\": [\"A\"]}",
                "{\"name\": \"Test\", \"kosten\": 1, \"varianten\": [{\"name\": \"X\", \"kosten\": 0}]}",
        };
        String[] invalidJsons = new String[]{
                "{\"name\": \"Test\", \"kosten\": -1}",
                "{\"name\": \"Test\", \"kosten\": 1, \"varianten\": [\"\"]}",
        };
        for (String json : validJsons) {
            assertValidJson("{\"sonderfertigkeiten\":[" + json + "]}");
        }
        for (String json : invalidJsons) {
            assertInvalidJson("{\"sonderfertigkeiten\":[" + json + "]}");
        }
    }

    @Test
    public void testBedingungValidation() {
        String[] validJsons = new String[]{
                "{\"type\": \"Sonderfertigkeit\", \"name\": \"SF1\"}",
        };
        String[] invalidJsons = new String[]{
                "{\"type\": null, \"name\": \"SF1\"}",
                "{\"type\": \"X\", \"name\": \"SF1\"}",
                "{\"type\": \"Sonderfertigkeit\"}",
                "{\"type\": \"or\", \"bedingungen\": [{\"type\": \"Sonderfertigkeit\"}]}",
        };
        for (String json : validJsons) {
            assertValidJson("{\"sonderfertigkeiten\":[{\"name\": \"A\", \"kosten\": 0, \"bedingungen\": [" + json + "]}]}");
        }
        for (String json : invalidJsons) {
            assertInvalidJson("{\"sonderfertigkeiten\":[{\"name\": \"A\", \"kosten\": 0, \"bedingungen\": [" + json + "]}]}");
        }
    }

    private static void assertValidJson(String json) {
        try (Reader r = new StringReader(json)) {
            Loader.loadFromJson(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertInvalidJson(String json) {
        try (Reader r = new StringReader(json)) {
            Loader.loadFromJson(r);
            throw new AssertionError("Invalid JSON should not have passed: " + json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Loader.ConfigError e) {
            System.out.println("Success: " + json + "\n => " + e.getMessage());
        }
    }
}
