package ru.sberbank.qlik.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QlikApiUtils {
    /**
     * ObjectMapper для сериализации и десериализации объектов
     */
    private static final ObjectMapper objectMapper = initObjectMapper();


    private static ObjectMapper initObjectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper getObjectMapper() {
        return QlikApiUtils.objectMapper;
    }
}
