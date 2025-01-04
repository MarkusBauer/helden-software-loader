package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.mb.heldensoftware.customentries.CsvConverter;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

public class Loader {

    public static class ConfigError extends RuntimeException {
        public ConfigError(String msg) {
            super(msg);
        }
    }

    public static enum FileType {
        JSON,
        YAML,
        CSV
    }

    public static Config load(Reader reader, FileType type) throws IOException {
        ObjectMapper mapper;
        if (type == FileType.JSON) {
            mapper = new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build());
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        } else if (type == FileType.YAML) {
            mapper = new ObjectMapper(YAMLFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build());
            mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        } else {
            throw new RuntimeException("Unsupported file type: " + type);
        }

        try {
            Config config = mapper.readValue(reader, Config.class);
            validate(config);
            return config;
        } catch (DatabindException e) {
            throw new ConfigError(e.getMessage());
        }
    }

    public static Config load(File file, FileType type) throws IOException {
        Reader reader = null;
        try {
            if (type == FileType.CSV) {
                // convert CSV to JSON stream
                reader = new InputStreamReader(new CsvConverter().convertToJson(file.toPath()), StandardCharsets.UTF_8);
            } else {
                reader = preprocessStream(Files.newInputStream(file.toPath()));
            }
            Config config = load(reader, FileType.JSON);
            config.source = file.getAbsolutePath();
            return config;

        } catch (ConfigError e) {
            throw new ConfigError("In " + file.getAbsolutePath() + ": \n" + e.getMessage());
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public static Config load(File file) throws IOException {
        if (file.getName().endsWith(".json") || file.getName().endsWith(".json.txt")) {
            return load(file, FileType.JSON);
        } else if (file.getName().endsWith(".yaml") || file.getName().endsWith(".yaml.txt")) {
            return load(file, FileType.YAML);
        } else if (file.getName().endsWith(".csv") || file.getName().endsWith(".csv.txt")) {
            return load(file, FileType.CSV);
        } else {
            return null;
        }
    }

    public static Config load(String data, FileType type) {
        try (Reader r = new StringReader(data)) {
            return Loader.load(r, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
