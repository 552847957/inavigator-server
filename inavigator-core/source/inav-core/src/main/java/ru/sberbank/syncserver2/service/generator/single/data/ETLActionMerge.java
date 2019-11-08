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
@XmlType(name = "merge")
public class ETLActionMerge {
    private int seriesIndex;
    private String fields;

    public ETLActionMerge() {
        this(0,null);
    }

    public ETLActionMerge(int seriesIndex, String fields) {
        this.seriesIndex = seriesIndex;
        this.fields = fields;
    }

    @XmlAttribute(name = "seriesIndex")
    public int getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(int seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    @XmlAttribute(name = "fields")
    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }
}
