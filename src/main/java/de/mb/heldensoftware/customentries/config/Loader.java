package de.mb.heldensoftware.customentries.config;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.*;
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
import java.util.ArrayList;
import java.util.Arrays;
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
            mapper.configOverride(ArrayList.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

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
        return load(bytesToString(content), type);
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

    private static String bytesToString(byte[] content) {
        // BOM check
        if (content.length >= 3 && content[0] == (byte) 0xEF && content[1] == (byte) 0xBB && content[2] == (byte) 0xBF) {
            return new String(Arrays.copyOfRange(content, 3, content.length), StandardCharsets.UTF_8);
        }
        return new String(content, detectEncoding(content));
    }

    /**
     * Automatic charset detection, based on jchardet. Typical charsets are windows-1252 and UTF-8.
     *
     * @param content File content to perform detection on
     * @return A charset, with UTF-8 being default.
     */
    private static Charset detectEncoding(byte[] content) {
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
