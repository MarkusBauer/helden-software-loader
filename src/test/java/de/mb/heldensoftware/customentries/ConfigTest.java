package de.mb.heldensoftware.customentries;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.imifou.jsonschema.module.addon.AddonModule;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import de.mb.heldensoftware.customentries.config.Config;
import de.mb.heldensoftware.customentries.config.Loader;
import org.junit.Test;

import java.io.*;

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

    @Test
    public void testSchema() throws FileNotFoundException, UnsupportedEncodingException {
        SchemaGeneratorConfigBuilder configBuilder =
                new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
                        .with(new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED))
                        .with(new AddonModule());
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(Config.class);

        System.out.println(jsonSchema.toPrettyString());
        try (PrintWriter writer = new PrintWriter("extensions.schema.json", "UTF-8")) {
            writer.println(jsonSchema.toPrettyString());
        }
    }
}
