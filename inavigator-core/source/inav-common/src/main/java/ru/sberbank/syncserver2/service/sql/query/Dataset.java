package ru.sberbank.syncserver2.service.sql.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Erin
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Dataset {

    @XmlElement(name = "R")
    private List<DatasetRow> rows;

    public Dataset() {
    }

    public List<DatasetRow> getRows() {
        return rows;
    }

    public void addRow(DatasetRow row) {
        if (rows == null) {
            rows = new ArrayList<DatasetRow>();
        }

        rows.add(row);
    }

    @Override
    public String toString() {
        return "Dataset [rows=" + rows + "]";
    }
}
