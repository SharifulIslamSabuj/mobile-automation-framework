package com.mobileautomation.framework.data.reader.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobileautomation.framework.data.reader.AbstractDataReader;

/** JSON test-data reader, backed by Jackson's {@link ObjectMapper}. */
public final class JsonDataReader extends AbstractDataReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ObjectMapper mapper() {
        return MAPPER;
    }

    @Override
    protected String formatName() {
        return "JSON";
    }
}
