package de.mb.heldensoftware.customentries.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.lang.reflect.Constructor;

public class SchemaCreator {
    public static String createSchema() {
        SchemaGeneratorConfigBuilder configBuilder =
                new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
                        .with(new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED))
                        .with(new AddonModule())
                        .with(new JavaxValidationModule(JavaxValidationOption.INCLUDE_PATTERN_EXPRESSIONS));
        configBuilder.forTypesInGeneral().withTypeAttributeOverride(SchemaCreator::fixTypesWithStringConstructor);
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(Config.class);

        return jsonSchema.toPrettyString();
    }

    public static void main(String[] args) {
        String schema = createSchema();
        System.out.println(schema);
        try (PrintWriter writer = new PrintWriter("extensions.schema.json", "UTF-8")) {
            writer.println(schema);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fixTypesWithStringConstructor(ObjectNode objectNode, TypeScope typeScope, SchemaGenerationContext schemaGenerationContext) {
        if (!objectNode.get("type").textValue().equals("object"))
            return;
        Class<?> cls = typeScope.getType().getErasedType();
        if (cls == String.class)
            return;
        // If an object can be constructed by a single string, then we'll accept a single string.
        // Happens: SFVariantConfig
        for (Constructor<?> c : cls.getConstructors()) {
            if (c.getParameterTypes().length == 1 && c.getParameterTypes()[0].equals(String.class)) {
                ArrayNode types = schemaGenerationContext.getGeneratorConfig().createArrayNode();
                types.add("string");
                types.add("object");
                objectNode.set("type", types);
            }
        }
    }
}
