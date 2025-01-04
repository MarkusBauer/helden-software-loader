package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.config.Config;
import de.mb.heldensoftware.customentries.config.Loader;
import de.mb.heldensoftware.customentries.config.ZauberConfig;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class ConfigTest {
    @Test
    public void testExampleJsonFile() {
        try (Reader r = Loader.preprocessStream(getClass().getResourceAsStream("/examples/examples.json"))) {
            Loader.load(r, Loader.FileType.JSON);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testExampleYamlFile() {
        try (Reader r = Loader.preprocessStream(getClass().getResourceAsStream("/examples/examples.yaml"))) {
            Loader.load(r, Loader.FileType.YAML);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testExampleConfigsEqual() throws IOException {
        Config json;
        Config yaml;
        try (Reader r = Loader.preprocessStream(getClass().getResourceAsStream("/examples/examples.json"))) {
            json = Loader.load(r, Loader.FileType.JSON);
        }
        try (Reader r = Loader.preprocessStream(getClass().getResourceAsStream("/examples/examples.yaml"))) {
            yaml = Loader.load(r, Loader.FileType.YAML);
        }

        assertEquals(json, yaml);
    }

    @Test
    public void testZauberDefaultValues() {
        String json = "{\"zauber\": [" +
                "{\"name\": \"Test\", \"kategorie\": \"F\", \"probe\": \"MU/KL/KL\", \"merkmale\": [\"Anti\"]}" +
                "]}";
        Config config = Loader.load(json, Loader.FileType.JSON);
        assertEquals(1, config.zauber.size());
        ZauberConfig zauber = config.zauber.get(0);
        assertEquals("Test", zauber.name);
        assertEquals("F", zauber.kategorie);
        assertEquals("Anti", zauber.merkmale.get(0));
        assertEquals("MU/KL/KL", zauber.probe);
        assertEquals("", zauber.mod);
        assertEquals("", zauber.quelle);
        assertEquals(1, zauber.settings.size());
        assertEquals("Alle", zauber.settings.get(0));
        assertEquals(0, zauber.spezialisierungen.size());
        assertEquals(0, zauber.verbreitung.size());
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
        Loader.load(json, Loader.FileType.JSON);
    }

    private static void assertInvalidJson(String json) {
        try {
            Loader.load(json, Loader.FileType.JSON);
            throw new AssertionError("Invalid JSON should not have passed: " + json);
        } catch (Loader.ConfigError e) {
            System.out.println("Success: " + json + "\n => " + e.getMessage());
        }
    }
}
