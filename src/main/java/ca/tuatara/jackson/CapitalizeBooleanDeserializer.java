package ca.tuatara.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CapitalizeBooleanDeserializer extends StdDeserializer<Boolean> {
    public CapitalizeBooleanDeserializer() {
        super(Boolean.TYPE);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public Boolean deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Boolean booleanValue;

        JsonToken t = jp.getCurrentToken();
        // If it is a String then accept capitalized
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if ("True".equals(text)) {
                return Boolean.TRUE;
            }
            if ("False".equals(text)) {
                return Boolean.FALSE;
            }
            if (text.length() == 0) {
                return (Boolean) getEmptyValue();
            }
        }

        booleanValue = _parseBoolean(jp, ctxt);
        return booleanValue;
    }
}
