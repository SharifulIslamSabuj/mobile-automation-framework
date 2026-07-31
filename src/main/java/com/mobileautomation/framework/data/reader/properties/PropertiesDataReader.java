package com.mobileautomation.framework.data.reader.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.mobileautomation.framework.data.reader.AbstractDataReader;

/** Properties test-data reader, backed by Jackson's {@link JavaPropsMapper} (an {@link ObjectMapper} subclass — same POJO-binding model as JSON/YAML, rather than hand-rolled {@code java.util.Properties}-to-POJO mapping). */
public final class PropertiesDataReader extends AbstractDataReader {

    private static final ObjectMapper MAPPER = new JavaPropsMapper();

    @Override
    protected ObjectMapper mapper() {
        return MAPPER;
    }

    @Override
    protected String formatName() {
        return "Properties";
    }
}
