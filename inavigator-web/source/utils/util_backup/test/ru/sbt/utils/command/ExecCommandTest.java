package ru.sbt.utils.command;

import ru.sbt.utils.db.DataBase;
import ru.sbt.utils.db.Table;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static ru.sbt.utils.command.BasicCommand.*;
import static ru.sbt.utils.db.SQLfactoryDB.createTables;
import static ru.sbt.utils.file.ScriptFile.readScriptFile;
import static ru.sbt.utils.property.PropertiesFactory.*;

public class ExecCommandTest {

    String fileProperties = "properties/prop.txt";
    DataBase dataBase;


    @org.junit.Test
    public void testBackupData() throws Exception {

        ExecCommand execCommand = new ExecCommand();

        Properties properties = getProperties(fileProperties);
        //  String sqlScript = "install.sql";

        String[] bases = getPropertyByNameDataBase(properties, "DB_NAME");
        String[] tables = getPropertyByNameTables(properties, "TABLES");
        String[] sqlScripts = properties.getProperty("SQL_SCRIPTS").split(",");
        String confserverDB = properties.getProperty("DB_CONFSERVER", "confserver").toUpperCase();

        String[] ordinaryTables = getPropertiesByNameEscapeValues(properties, "TABLES", "CONFSERVER_PROPERTY_VALUES");
        String[] tempTables;

        for (int indBase = 0; indBase < bases.length; indBase++)
         //int indBase = 0;
        {
            dataBase = new DataBase(setListArgsDBConnect(indBase, properties));
            createTables(dataBase.getConnection(), readScriptFile(sqlScripts[indBase]));

            if (bases[indBase].toUpperCase().compareTo(confserverDB.toUpperCase()) != 0) {
                tempTables = ordinaryTables;
            } else {
                tempTables = tables;
            }


            insertControlValueToTables(tempTables, dataBase);

            Table[] listTables = getTablesByNames(tempTables, dataBase);

            execCommand.backupTablesDB(indBase, properties, tempTables);

            createTables(dataBase.getConnection(), readScriptFile(sqlScripts[indBase]));
            execCommand.restoreTablesDB(indBase, properties, tempTables);
            Table[] controlTable = getTablesByNames(tempTables, dataBase);
            assertEquals("Equal error for db " + bases[indBase], compareTables(tempTables, listTables, controlTable), true);

            System.out.println("compare for " + bases[indBase] + " completed");
        }
    }


    /*@org.junit.Test
    public void testRestoreData() {

        fileProperties = "properties/properties.txt";
        Properties properties = PropertiesFactory.getProperties(fileProperties);


        String[] bases = PropertiesFactory.getPropertyByNameTables(properties, "DB_NAME");
        String[] tables = PropertiesFactory.getPropertyByNameTables(properties, "TABLES");

        for (int indBase = 0; indBase < bases.length; indBase++) {
            dataBase = new DataBase(PropertiesFactory.setListArgsDBConnect(indBase, properties));
            for (String iterTables : tables) {

                createTables(dataBase.getConnection(), readScriptFile("1.install.sql"));

                //Table table1 = SQLQueries.getSelectTableQuery(iterTables, dataBase.getConnection());
            }
        }


    }*/


}