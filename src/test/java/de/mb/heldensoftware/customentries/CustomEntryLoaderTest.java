package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.config.Loader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class CustomEntryLoaderTest {

    @Test
    public void testExampleJsonFile() {
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream("/examples/examples.json"))) {
            new CustomEntryLoader().loadCustomEntries(Loader.load(r, Loader.FileType.JSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}