package ru.sberbank.syncserver2.service.generator.single.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Created by Admin on 05.04.14.
 */
@XmlRootElement(name = "etl-database-list",namespace = "")
public class ETLDatabaseList {
    private ArrayList<ETLDatabase> databases = new ArrayList<ETLDatabase>();

    @XmlElement(name = "etl-database")
    public ArrayList<ETLDatabase> getDatabases() {
        return databases;
    }

    public void setDatabases(ArrayList<ETLDatabase> databases) {
        this.databases = databases;
    }
}
