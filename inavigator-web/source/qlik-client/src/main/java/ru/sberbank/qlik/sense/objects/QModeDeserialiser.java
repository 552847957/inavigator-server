package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class QModeDeserialiser extends JsonDeserializer<QMode> {
    @Override
    public QMode deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String valueAsString = jsonParser.getValueAsString();
        if ("P".equals(valueAsString)) {
            return QMode.DATA_MODE_PIVOT;
        } else if ("K".equals(valueAsString)) {
            return QMode.DATA_MODE_PIVOT_STACK;
        } else if ("T".equals(valueAsString)) {
            return QMode.DATA_MODE_TREE;
        } else {
            return QMode.DATA_MODE_STRAIGHT;
        }
    }
}
