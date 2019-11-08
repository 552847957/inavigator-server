package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlElement;


/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 22.11.11
 * Time: 18:02
 * To change this template use File | Settings | File Templates.
 */
public class RDRow implements Cloneable{
    private ArrayList<String> fields;

    public RDRow() {
    }

    public RDRow(String[] values) {
        fields = new ArrayList<String>(values.length);
        for (int i = 0; i < values.length; i++) {
            fields.add(values[i]);
        }
    }

    public RDRow(ArrayList<String> fields) {
        this.fields = fields;
    }

    @XmlElement(name = "field")
    public ArrayList<String> getFields() {
        return fields;
    }

    /**
     * @param index
     * @return Field value or null if field's index is out of bounds
     */
    public String getField(int index) {
        String value = null;

        if (fields != null && index >= 0 && index < fields.size()) {
            value = fields.get(index);
        }

        return value;
    }

    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("    <row>\n");
        for (Iterator<String> iterator = fields.iterator(); iterator.hasNext(); ) {
            String value =  iterator.next();
            sb.append("      <field>"+value+"</field>\n");
        }
        sb.append("    </row>");
        return sb.toString();
    }

    @Override
    public Object clone() {
        RDRow result = null;    //To change body of overridden methods use File | Settings | File Templates.
        try {
            result = (RDRow) super.clone();
            if(fields!=null){
                result.fields = new ArrayList<String>(fields);
            }
            return result;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDRow rdRow = (RDRow) o;

        if (fields != null ? !fields.equals(rdRow.fields) : rdRow.fields != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fields != null ? fields.hashCode() : 0;
    }

    public String getFieldValue(int index){
        return fields.get(index);
    }

    public void setFieldValue(int index, String value) {
        fields.set(index, value);
    }


}
