package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.config.Config;
import de.mb.heldensoftware.customentries.config.Loader;
import de.mb.heldensoftware.customentries.config.ZauberConfig;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ConfigTest {
    @Test
    public void testExampleJsonFile() throws IOException, URISyntaxException {
        Path p = Paths.get(getClass().getResource("/examples/examples.json").toURI());
        Loader.load(p, Loader.FileType.JSON);
    }

    @Test
    public void testExampleYamlFile() throws IOException, URISyntaxException {
        Path p = Paths.get(getClass().getResource("/examples/examples.yaml").toURI());
        Loader.load(p, Loader.FileType.YAML);
    }

    @Test
    public void testExampleConfigsEqual() throws IOException, URISyntaxException {
        Path p = Paths.get(getClass().getResource("/examples/examples.json").toURI());
        Config json = Loader.load(p, Loader.FileType.JSON);
        p = Paths.get(getClass().getResource("/examples/examples.yaml").toURI());
        Config yaml = Loader.load(p, Loader.FileType.YAML);

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

    private static final byte[] BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] UTF8_CHARS = new byte[]{(byte) 0xc3, (byte) 0xa4, (byte) 0xc3, (byte) 0xb6, (byte) 0xc3, (byte) 0xbc};

    private static byte[] joinArrays(byte[]... arrays) {
        int len = 0;
        for (byte[] array : arrays) {
            len += array.length;
        }
        byte[] target = new byte[len];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, target, pos, array.length);
            pos += array.length;
        }
        return target;
    }

    @Test
    public void testBomYaml() {
        String s1 = "sonderfertigkeiten:\n- name: \"";
        String s2 = "\"\n  kosten: 10";
        byte[] content = joinArrays(BOM, s1.getBytes(StandardCharsets.UTF_8), UTF8_CHARS, s2.getBytes(StandardCharsets.UTF_8));
        Config config = Loader.load(content, Loader.FileType.YAML);
        assertEquals(new String(UTF8_CHARS, StandardCharsets.UTF_8), config.sonderfertigkeiten.get(0).name);
    }

    @Test
    public void testBomJson() {
        String s1 = "{\"sonderfertigkeiten\":[{\"name\": \"";
        String s2 = "\", \"kosten\": 10}]}";
        byte[] content = joinArrays(BOM, s1.getBytes(StandardCharsets.UTF_8), UTF8_CHARS, s2.getBytes(StandardCharsets.UTF_8));
        Config config = Loader.load(content, Loader.FileType.JSON);
        assertEquals(new String(UTF8_CHARS, StandardCharsets.UTF_8), config.sonderfertigkeiten.get(0).name);
    }

    @Test
    public void testBomCsv() {
        String s1 = "Name,Kategorie,Merkmale,Probe,Mods/MR,Verbreitung,Settings\n";
        String s2 = ",A,Anti,KL/IN/IN,,,Alle\n";
        byte[] content = joinArrays(BOM, s1.getBytes(StandardCharsets.UTF_8), UTF8_CHARS, s2.getBytes(StandardCharsets.UTF_8));
        Config config = Loader.load(content, Loader.FileType.CSV);
        assertEquals(new String(UTF8_CHARS, StandardCharsets.UTF_8), config.zauber.get(0).name);
    }
}
