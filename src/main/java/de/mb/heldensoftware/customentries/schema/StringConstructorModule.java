package de.mb.heldensoftware.customentries.schema;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;

import java.lang.reflect.Constructor;


/**
 * Fix a pattern where a class can be deserialized from either a string or an object.
 * Schema generator picks up the object part only and omits the constructor(String).
 */
public class StringConstructorModule implements Module {
    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder schemaGeneratorConfigBuilder) {
        schemaGeneratorConfigBuilder.forTypesInGeneral().withTypeAttributeOverride(this::fixTypesWithStringConstructor);
    }

    private void fixTypesWithStringConstructor(ObjectNode objectNode, TypeScope typeScope, SchemaGenerationContext schemaGenerationContext) {
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
