package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.config.Loader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class ConfigTest {
    @Test
    public void testExampleFile() {
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream("/examples/examples.json"))) {
            Loader.loadFromJson(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testOptionals() {
        String json = "{\"zauber\": [{\"name\": \"Test\"}]}";
        try (Reader r = new StringReader(json)) {
            Loader.loadFromJson(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
