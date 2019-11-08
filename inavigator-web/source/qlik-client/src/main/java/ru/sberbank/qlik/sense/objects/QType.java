package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = QTypeDeserializer.class)
public enum QType {
    DOC,
    BARCHART,
    SHEET,
    PIVOT_TABLE,
    LISTBOX,
    GENERIC_OBJECT;
}

class QTypeDeserializer extends JsonDeserializer<QType> {
    @Override
    public QType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getValueAsString();
        if ("GenericObject".equals(value)) {
            return QType.GENERIC_OBJECT;
        } else if ("barchart".equals(value)) {
            return QType.BARCHART;
        } else if ("pivot-table".equals(value)) {
            return QType.PIVOT_TABLE;
        } else if ("Doc".equals(value)) {
            return QType.DOC;
        } else if ("sheet".equals(value)) {
            return QType.SHEET;
        } else if ("listbox".equals(value)) {
            return QType.LISTBOX;
        } else {
            throw new RuntimeException("Unexpected type " + value);
        }
    }
}
