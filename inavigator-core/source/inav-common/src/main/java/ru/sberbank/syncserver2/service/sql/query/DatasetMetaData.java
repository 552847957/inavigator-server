package ru.sberbank.syncserver2.service.sql.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Erin
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DatasetMetaData {

    @XmlAttribute(name = "RowCount")
    private Integer rowCount;

    @XmlElement(name = "Field")
    private List<DatasetFieldMetaData> fields;

    public DatasetMetaData() {
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public List<DatasetFieldMetaData> getFields() {
        return fields;
    }

    public void addField(DatasetFieldMetaData field) {
        if (fields == null) {
            fields = new ArrayList<DatasetFieldMetaData>();
        }

        fields.add(field);
    }

    @Override
    public String toString() {
        return "DatasetMetaData [rowCount=" + rowCount + ", fields=" + fields + "]";
    }

}
