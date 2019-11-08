package ru.sberbank.qlik.services;


import java.util.ArrayList;
import java.util.List;

/**
 * Данные объекта
 */
public class ObjectData {
    /**
     * ID объекта
     */
    private String id;
    /**
     * Информация о строках
     */
    private Dimension left;
    /**
     * Информация о столбцах
     */
    private Dimension top;
    private List<Measure> measures = new ArrayList<Measure>();
    /**
     * Значения
     */
    private List<Value> values = new ArrayList<Value>();
    /**
     * При получении данных произошла ошибка
     */
    private boolean error;
    /**
     * Тип объекта
     */
    private String type;
    private String errorMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Dimension getLeft() {
        return left;
    }

    public void setLeft(Dimension left) {
        this.left = left;
    }

    public Dimension getTop() {
        return top;
    }

    public void setTop(Dimension top) {
        this.top = top;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean getError() {
        return error;
    }
}
