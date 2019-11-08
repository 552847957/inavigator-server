package ru.sberbank.qlik.view;

public class IFieldValue {
    private Double Number;
    private boolean IsNumeric;
    private String Text;

    public Double getNumber() {
        return Number;
    }

    public void setNumber(Double number) {
        Number = number;
    }

    public boolean isNumeric() {
        return IsNumeric;
    }

    public void setNumeric(boolean numeric) {
        IsNumeric = numeric;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }
}
