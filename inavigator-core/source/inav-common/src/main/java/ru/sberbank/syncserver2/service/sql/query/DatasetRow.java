package ru.sberbank.syncserver2.service.sql.query;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Erin
 *
 */
public class DatasetRow {

    @XmlElement(name = "F")
    private List<String> values;

    public DatasetRow() {
    }

    public List<String> getValues() {
        return values;
    }

    public void addValue(String value) {
        if (values == null) {
            values = new ArrayList<String>();
        }
        values.add(value);
    }

    @Override
    public String toString() {
        return "DatasetRow [values=" + values + "]";
    }

}
