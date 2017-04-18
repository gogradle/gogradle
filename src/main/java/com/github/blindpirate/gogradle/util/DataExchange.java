package com.github.blindpirate.gogradle.util;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static com.ctc.wstx.api.WstxInputProperties.PARSING_MODE_DOCUMENTS;

public class DataExchange {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static XmlMapper xmlMapper;

    static {
        // Override the default config, otherwise it will not ignore <?xml ...>
        WstxInputFactory inputFactory = new WstxInputFactory();
        inputFactory.getConfig().setInputParsingMode(PARSING_MODE_DOCUMENTS);
        xmlMapper = new XmlMapper(inputFactory);
    }

    public static <T> T parseJson(File file, Class<T> clazz) {
        return parseWithMapper(JSON_MAPPER, file, clazz);
    }

    public static <T> T parseJson(String str, Class<T> clazz) {
        return parseWithMapper(JSON_MAPPER, str, clazz);
    }

    public static <T> T parseYaml(File file, Class<T> clazz) {
        return parseWithMapper(YAML_MAPPER, file, clazz);
    }

    public static <T> T parseYaml(String s, Class<T> clazz) {
        return parseWithMapper(YAML_MAPPER, s, clazz);
    }

    public static <T> T parseXml(File file, Class<T> clazz) {
        return parseWithMapper(xmlMapper, file, clazz);
    }

    private static <T> T parseWithMapper(ObjectMapper mapper, String s, Class<T> clazz) {
        try {
            return mapper.readValue(s, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> T parseWithMapper(ObjectMapper mapper, File file, Class<T> clazz) {
        try {
            return mapper.readValue(file, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toYaml(Object model) {
        try {
            return YAML_MAPPER.writeValueAsString(model);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
