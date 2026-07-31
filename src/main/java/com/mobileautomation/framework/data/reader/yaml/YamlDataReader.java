package com.mobileautomation.framework.data.reader.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mobileautomation.framework.data.reader.AbstractDataReader;

/** YAML test-data reader, backed by Jackson's {@link YAMLMapper} (an {@link ObjectMapper} subclass — same POJO-binding model as JSON). */
public final class YamlDataReader extends AbstractDataReader {

    private static final ObjectMapper MAPPER = new YAMLMapper();

    @Override
    protected ObjectMapper mapper() {
        return MAPPER;
    }

    @Override
    protected String formatName() {
        return "YAML";
    }
}
