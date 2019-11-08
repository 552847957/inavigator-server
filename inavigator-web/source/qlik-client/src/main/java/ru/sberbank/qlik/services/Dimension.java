package ru.sberbank.qlik.services;



import java.util.ArrayList;
import java.util.List;


public class Dimension {
    private String label;
    private List<String> values = new ArrayList<String>();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
