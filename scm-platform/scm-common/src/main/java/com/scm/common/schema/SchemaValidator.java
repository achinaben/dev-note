package com.scm.common.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.InputStream;
import java.util.Set;

public class SchemaValidator {
    private final JsonSchema schema;

    public SchemaValidator(InputStream schemaStream) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.schema = factory.getSchema(schemaStream);
    }

    public void validateOrThrow(JsonNode node) {
        Set<ValidationMessage> errors = schema.validate(node);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Schema validation failed: " + errors);
        }
    }
}
