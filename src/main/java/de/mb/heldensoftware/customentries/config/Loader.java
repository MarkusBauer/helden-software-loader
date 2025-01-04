package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Loader {

    public static class ConfigError extends RuntimeException {
        public ConfigError(String msg) {
            super(msg);
        }
    }

    public static Config loadFromJson(Reader reader) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        try {
            Config config = mapper.readValue(reader, Config.class);
            validate(config);
            return config;
        } catch (DatabindException e) {
            throw new ConfigError(e.getMessage());
        }
    }

    public static Config loadFromYaml(Reader reader) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        try {
            Config config = mapper.readValue(reader, Config.class);
            validate(config);
            return config;
        } catch (DatabindException e) {
            throw new ConfigError(e.getMessage());
        }
    }

    /**
     * Try to guess the encoding of an input stream, returning a proper Reader (for load... methods)
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static Reader preprocessStream(InputStream is) throws IOException {
        BufferedInputStream input = new BufferedInputStream(is);
        // Remove BOM - I know my Windows users
        byte[] buffer = new byte[3];
        input.mark(4);
        input.read(buffer);
        if (!(buffer[0] == (byte) 0xEF && buffer[1] == (byte) 0xBB && buffer[2] == (byte) 0xBF)) {
            input.reset();
        }
        // Read file as UTF-8
        return new InputStreamReader(input, StandardCharsets.UTF_8);
    }

    public static void validate(Config config) {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            throwViolations(validator.validate(config));
        }
    }

    private static <T> void throwViolations(Set<ConstraintViolation<T>> violations) {
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder("Config ist nicht gültig:");
            for (ConstraintViolation<T> violation : violations) {
                msg.append("\n- ").append(violationToMessage(violation));
            }
            throw new ConfigError(msg.toString());
        }
    }

    private static <T> String violationToMessage(ConstraintViolation<T> violation) {
        String s = "\"" + violation.getPropertyPath() + "\" " + violation.getMessage();
        if (violation.getInvalidValue() != null) {
            s += " (gegeben: " + violation.getInvalidValue() + ")";
        } else {
            s += " (nicht angegeben)";
        }
        return s;
    }

}
