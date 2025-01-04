package de.mb.heldensoftware.customentries.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.imifou.jsonschema.module.addon.AddonModule;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationModule;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationOption;
import de.mb.heldensoftware.customentries.config.Config;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class SchemaCreator {
    public static String createSchema() {
        // This config is mostly duplicated in pom.xml
        SchemaGeneratorConfigBuilder configBuilder =
                new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
                        .with(new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED))
                        .with(new AddonModule())
                        .with(new JavaxValidationModule(JavaxValidationOption.INCLUDE_PATTERN_EXPRESSIONS))
                        .with(new StringConstructorModule());
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(Config.class);

        return jsonSchema.toPrettyString();
    }

    public static void main(String[] args) {
        String schema = createSchema();
        System.out.println(schema);
        try (PrintWriter writer = new PrintWriter("erweiterungen.schema.json", "UTF-8")) {
            writer.println(schema);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
