package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.config.Config;
import de.mb.heldensoftware.customentries.config.Loader;
import de.mb.heldensoftware.customentries.config.ZauberConfig;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class CsvConfigTest {
    private void testImportedZauber(Path p) throws IOException {
        Config config = Loader.load(p, Loader.FileType.CSV);
        assertEquals(1, config.zauber.size());
        for (ZauberConfig z : config.zauber) {
            // Name
            assertTrue(z.name.startsWith("A Testzauber"));
            // Special character decoding
            assertTrue(z.name.endsWith("äöü"));
            // Comma-separated lists
            assertEquals(2, z.verbreitung.get("Hex").intValue());
            // Empty strings
            assertEquals("", z.mod);
            // Assert no trailing \r has been left
            assertEquals(1, z.settings.size());
            assertEquals("Aventurien", z.settings.get(0));
        }
    }

    @Test
    public void testExampleCsvLibreoffice() throws URISyntaxException, IOException {
        Path p = Paths.get(getClass().getResource("/examples/erweiterungen-libreoffice.csv").toURI());
        testImportedZauber(p);
    }


    @Test
    public void testExampleCsvExcel() throws URISyntaxException, IOException {
        Path p = Paths.get(getClass().getResource("/examples/erweiterungen-excel-2.csv").toURI());
        testImportedZauber(p);
    }

    @Test
    public void testExampleCsvExcelStrange() throws URISyntaxException, IOException {
        Path p = Paths.get(getClass().getResource("/examples/erweiterungen-excel-strange.csv").toURI());
        testImportedZauber(p);
    }


    @Test
    public void testExampleCsv() throws URISyntaxException, IOException {
        // Excel-style, UTF8, BOM
        Path p = Paths.get(getClass().getResource("/examples/erweiterungen.csv").toURI());
        testImportedZauber(p);
    }
}
