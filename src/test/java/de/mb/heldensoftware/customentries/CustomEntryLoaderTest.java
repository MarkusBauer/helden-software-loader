package de.mb.heldensoftware.customentries;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class CustomEntryLoaderTest {

    @Test
    public void testExampleFile() {
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream("/examples/examples.json"))){
            new CustomEntryLoader().loadCustomEntries(r);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

}