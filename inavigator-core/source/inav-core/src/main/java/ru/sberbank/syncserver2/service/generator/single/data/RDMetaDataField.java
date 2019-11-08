package ru.sberbank.syncserver2.service.generator.single.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 22.11.11
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class RDMetaDataField implements Cloneable{

    public static final String STRING_DATA_TYPE = "String";
    public static final String LONG_DATA_TYPE = "long";
    public static final String DOUBLE_DATA_TYPE = "double";
    public static final String DATETIME_DATA_TYPE = "datetime";
    public static final String STRING_ARRAY_DATA_TYPE = "stringArray";
    public static final String VALUE_ARRAY_DATA_TYPE = "valueArray";
    public static final String DATE_ARRAY_DATA_TYPE = "dateArray";

    private String name;
    private String dataType;

    @XmlTransient
    private String caption;

    public RDMetaDataField() {
    }

    public RDMetaDataField(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
        this.caption = "";
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "dataType")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @XmlTransient
    //@XmlAttribute(name = "caption")
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString(){
        return "      <field name=\""+name+"\" dataType=\""+dataType+"\" caption=\""+caption+"\"/>";
    }

    @Override
    public Object clone()  {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

}
