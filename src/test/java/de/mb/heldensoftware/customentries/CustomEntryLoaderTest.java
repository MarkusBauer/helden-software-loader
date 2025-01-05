package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.config.Loader;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomEntryLoaderTest {

    @Test
    public void testExampleJsonFile() throws URISyntaxException, IOException {
        Path p = Paths.get(getClass().getResource("/examples/examples.json").toURI());
        new CustomEntryLoader().loadCustomEntries(Loader.load(p, Loader.FileType.JSON));
    }

}