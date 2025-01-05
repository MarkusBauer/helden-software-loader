package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.mb.heldensoftware.customentries.CsvConverter;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static Config load(String content, FileType type) {
        if (type == FileType.CSV)
            return new CsvConverter().convertToConfig(content);

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
            Config config = mapper.readValue(content, Config.class);
            validate(config);
            return config;
        } catch (JsonProcessingException e) {
            throw new ConfigError(e.getMessage());
        }
    }

    public static Config load(byte[] content, FileType type) {
        return load(new String(content, detectEncoding(content)), type);
    }

    public static Config load(Path p, FileType type) throws IOException {
        Config config = load(Files.readAllBytes(p), type);
        config.source = p.toAbsolutePath().toString();
        return config;
    }

    public static Config load(Path p) throws IOException {
        String fname = p.getFileName().toString();
        if (fname.endsWith(".json") || fname.endsWith(".json.txt")) {
            return load(p, FileType.JSON);
        } else if (fname.endsWith(".yaml") || fname.endsWith(".yaml.txt")) {
            return load(p, FileType.YAML);
        } else if (fname.endsWith(".csv") || fname.endsWith(".csv.txt")) {
            return load(p, FileType.CSV);
        } else {
            return null;
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
        // TODO remove
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

    /**
     * Automatic charset detection, based on jchardet. Typical charsets are windows-1252 and UTF-8.
     *
     * @param content File content to perform detection on
     * @return A charset, with UTF-8 being default.
     */
    private static Charset detectEncoding(byte[] content) {
        // TODO test BOM
        // What an ugly library interface. Directly from hell...
        final Charset[] result = new Charset[]{StandardCharsets.UTF_8};
        nsDetector detector = new nsDetector();
        detector.Init(new nsICharsetDetectionObserver() {
            @Override
            public void Notify(String s) {
                result[0] = Charset.forName(s);
            }
        });
        try {
            detector.DoIt(content, content.length, false);
            detector.DataEnd();
            detector.Done();
        } catch (UnsupportedCharsetException e) {
            System.err.println(e.getMessage());
        }
        return result[0];
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
