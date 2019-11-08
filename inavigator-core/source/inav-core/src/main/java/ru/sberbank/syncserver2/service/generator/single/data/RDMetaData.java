package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;


/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 22.11.11
 * Time: 17:32
 * To change this template use File | Settings | File Templates.
 */
public class RDMetaData implements Cloneable{
    private ArrayList<RDMetaDataField> fields = new ArrayList<RDMetaDataField>();

    public RDMetaData() {
    }

    public RDMetaData(ArrayList<RDMetaDataField> fields) {
        this.fields = fields;
    }

    @XmlElement(name = "field")
    public ArrayList<RDMetaDataField> getFields() {
        return fields;
    }

    public void setFields(ArrayList<RDMetaDataField> fields) {
        this.fields = fields;
    }

    @Override
    public String toString(){
        return "<metadata>"+ RDXMLHelper.toString(fields)+"    </metadata>";
    }

    @Override
    public Object clone()  {
        try {
            RDMetaData result = (RDMetaData) super.clone();    //To change body of overridden methods use File | Settings | File Templates.
            if(this.fields!=null){
                result.fields = new ArrayList<RDMetaDataField>(this.fields.size());
                for (int i = 0; i < fields.size(); i++) {
                    RDMetaDataField f = (RDMetaDataField) fields.get(i).clone();
                    result.fields.add(f);
                }
            }
            return result;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    public int getFieldIndexByName(String field) {
        if(field==null){
            return -1;
        }
        for (int i = 0; i < fields.size(); i++) {
            RDMetaDataField rdMetaDataField =  fields.get(i);
            if(field.equals(rdMetaDataField.getName())){
                return i;
            }
        }
        return -1;  //To change body of created methods use File | Settings | File Templates.
    }

    public RDMetaDataField getFieldByName(String field) {
        if(field==null){
            return null;
        }
        for (int i = 0; i < fields.size(); i++) {
            RDMetaDataField rdMetaDataField =  fields.get(i);
            if(field.equals(rdMetaDataField.getName())){
                return (RDMetaDataField) rdMetaDataField.clone();
            }
        }
        return null;
    }

    public RDMetaDataField getFieldByIndex(int index) {
        RDMetaDataField rdMetaDataField =  fields.get(index);
        return (RDMetaDataField) rdMetaDataField.clone();

    }

    public void addField(String name, String dataType, String caption) {
        RDMetaDataField field = new RDMetaDataField();
        field.setName(name);
        field.setDataType(dataType);
        field.setCaption(caption);
        fields.add(field);
    }
}
