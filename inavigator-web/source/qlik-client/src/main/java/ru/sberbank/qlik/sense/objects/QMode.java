package ru.sberbank.qlik.sense.objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = QModeDeserialiser.class)
public enum QMode {
    DATA_MODE_PIVOT, DATA_MODE_PIVOT_STACK, DATA_MODE_TREE, DATA_MODE_STRAIGHT
}


