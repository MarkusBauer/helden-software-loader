package de.mb.heldensoftware.customentries;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class CustomEntryLoaderTest {

	@Test
	public void testExampleFile() {
		try (Reader r = new InputStreamReader(getClass().getResourceAsStream("/examples/examples.json"))) {
			new CustomEntryLoader().loadCustomEntries(r);
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}


	private void testImportedZauber(Reader in) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject customEntries = (JSONObject) parser.parse(in);
		JSONArray zauber = (JSONArray) customEntries.get("zauber");
		assertEquals(1, zauber.size());
		for (Object o : zauber) {
			JSONObject z = (JSONObject) o;
			// Name
			assertTrue(((String) z.get("name")).startsWith("A Testzauber"));
			// Special character decoding
			assertTrue(((String) z.get("name")).endsWith("äöü"));
			// Comma-separated lists
			assertEquals(2L, ((JSONObject) z.get("verbreitung")).get("Hex"));
			// Empty strings
			assertEquals("", z.get("mod"));
			// Assert no trailing \r has been left
			assertEquals(1, ((JSONArray) z.get("settings")).size());
			assertEquals("Aventurien", ((JSONArray) z.get("settings")).get(0));
		}
	}

	@Test
	public void testExampleCsvLibreoffice() throws URISyntaxException, IOException, ParseException {
		Path p = Paths.get(getClass().getResource("/examples/erweiterungen-libreoffice.csv").toURI());
		try (InputStreamReader r = new InputStreamReader(new CsvConverter().convertToJson(p))) {
			testImportedZauber(r);
		}
	}


	@Test
	public void testExampleCsvExcel() throws URISyntaxException, IOException, ParseException {
		Path p = Paths.get(getClass().getResource("/examples/erweiterungen-excel-2.csv").toURI());
		try (InputStreamReader r = new InputStreamReader(new CsvConverter().convertToJson(p))) {
			testImportedZauber(r);
		}
	}

	@Test
	public void testExampleCsvExcelStrange() throws URISyntaxException, IOException, ParseException {
		Path p = Paths.get(getClass().getResource("/examples/erweiterungen-excel-strange.csv").toURI());
		try (InputStreamReader r = new InputStreamReader(new CsvConverter().convertToJson(p))) {
			testImportedZauber(r);
		}
	}


	@Test
	public void testExampleCsv() throws URISyntaxException, IOException, ParseException {
		// Excel-style, UTF8, BOM
		Path p = Paths.get(getClass().getResource("/examples/erweiterungen.csv").toURI());
		try (InputStreamReader r = new InputStreamReader(new CsvConverter().convertToJson(p))) {
			testImportedZauber(r);
		}
	}

}