package ru.sberbank.syncserver2.service.generator.single.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 02.02.12
 * Time: 19:35
 * To change this template use File | Settings | File Templates.
 */
@XmlType(name = "change-type")
public class ETLActionChangeType {
    private int seriesIndex;
    private String fields;
    private String newDataType;
    private Integer scale;

    public ETLActionChangeType() {
        this(0,null,null);
    }

    public ETLActionChangeType(int seriesIndex, String fields, String newDataType) {
        this.seriesIndex = seriesIndex;
        this.fields = fields;
        this.newDataType = newDataType;
    }

    @XmlAttribute(name = "seriesIndex")
    public int getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(int seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    @XmlAttribute(name="fields")
    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }


    @XmlAttribute(name="newDataType")
    public String getNewDataType() {
        return newDataType;
    }

    public void setNewDataType(String newType) {
        this.newDataType = newType;
    }

    @XmlAttribute(name="scale")
    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    @Override
    public String toString() {
        return "ETLActionChangeType [seriesIndex=" + seriesIndex + ", fields=" + fields + ", newDataType="
                + newDataType + ", scale=" + scale + "]";
    }
}
