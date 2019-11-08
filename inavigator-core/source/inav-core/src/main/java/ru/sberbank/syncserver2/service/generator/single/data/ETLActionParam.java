package ru.sberbank.syncserver2.service.generator.single.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 23.02.2012
 * Time: 13:40:07
 * To change this template use File | Settings | File Templates.
 */
public class ETLActionParam {
    private String name;
    private int    index;
    private String query;
    private String constValue;
    private String stopValue;
    private transient String value;

    @XmlAttribute (name="name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute (name="index")
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @XmlAttribute (name="query")
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlAttribute (name="stopValue")
    public String getStopValue() {
        return stopValue;
    }

    public void setStopValue(String stopValue) {
        this.stopValue = stopValue;
    }

    @XmlAttribute (name="constValue")
    public String getConstValue() {
        return constValue;
    }

    public void setConstValue(String constValue) {
        this.constValue = constValue;
    }
}
