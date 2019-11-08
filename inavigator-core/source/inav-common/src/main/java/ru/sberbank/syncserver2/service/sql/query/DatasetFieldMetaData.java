package ru.sberbank.syncserver2.service.sql.query;

import javax.xml.bind.annotation.*;

/**
 * @author Sergey Erin
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "type",
        "name"
    })
@XmlRootElement(name = "Field")
public class DatasetFieldMetaData {

    @XmlAttribute(name = "Name")
    private String name;

    @XmlAttribute(name = "Type")
    private FieldType type;

    public DatasetFieldMetaData() {
    }

    /**
     * @param name
     * @param type
     */
    public DatasetFieldMetaData(String name, FieldType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "DatasetFieldMetaData [name=" + name + ", type=" + type + "]";
    }

}
