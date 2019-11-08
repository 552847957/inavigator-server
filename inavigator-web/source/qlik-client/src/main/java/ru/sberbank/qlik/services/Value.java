package ru.sberbank.qlik.services;

import java.util.List;

/**
 * Значение
 */
public class Value {
    /**
     * Строковое значение
     */
    private String sValue;
    /**
     * Числовое значение
     */
    private Double nValue;
    /**
     * Вложенные значения
     */
    private List<Value> values;

    public Value() {
    }

    public String getSValue() {
        return this.sValue;
    }

    public Double getNValue() {
        return this.nValue;
    }

    public List<Value> getValues() {
        return this.values;
    }

    public void setSValue(String sValue) {
        this.sValue = sValue;
    }

    public void setNValue(Double nValue) {
        this.nValue = nValue;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }
}
