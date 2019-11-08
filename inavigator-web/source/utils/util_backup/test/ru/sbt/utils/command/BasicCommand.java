package ru.sbt.utils.command;

import ru.sbt.utils.db.DataBase;
import ru.sbt.utils.db.EqualTable;
import ru.sbt.utils.db.SQLQueries;
import ru.sbt.utils.db.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.sbt.utils.command.ControlValues.getValuesByTable;
import static ru.sbt.utils.db.SQLQueries.insertByPreparedStatment;
import static ru.sbt.utils.db.SQLQueries.queryForPreStatementForInsert;

/**
 * Created by akv on 19.11.2014.
 */
public class BasicCommand {


    public static void insertRowToTable(DataBase dataBase, String nameTable) {
        insertByPreparedStatment(dataBase.getConnection(), queryForPreStatementForInsert(nameTable), getValuesByTable(nameTable));
    }

    public static Table[] getTablesByNames(String[] nameTables, DataBase dataBase) {

        Table[] listTable;
        int indTable = 0;
//        if(dataBase.getConfServer()){
//            listTable = new Table[nameTables.length+1];
//
//        }else
        {
            listTable = new Table[nameTables.length];

        }
        for (String nameTable : nameTables) {
            Table table1 = SQLQueries.getSelectTableQuery(nameTable, dataBase.getConnection());
            listTable[indTable] = table1;
            indTable++;
        }

//        if(dataBase.getConfServer()){
//            listTable[nameTables.length] = SQLQueries.getSelectTableQuery("CONFSERVER_VERSIONS", dataBase.getConnection());
//            indTable++;
//        }

        return listTable;
    }


    public static void insertControlValueToTables(String[] nameTables, DataBase dataBase) {

        for (String nameTable : nameTables) {
            insertByPreparedStatment(dataBase.getConnection(), queryForPreStatementForInsert(nameTable), getValuesByTable(nameTable));
        }

       /* if(dataBase.getConfServer()){
            insertByPreparedStatment(dataBase.getConnection(), queryForPreStatementForInsert("CONFSERVER_VERSIONS"), getValuesByTable("CONFSERVER_VERSIONS"));
        }*/

    }


    public static boolean compareTables(String[] tableNames, Table[] tables1, Table[] tables2) {
        if (tables1.length != tables2.length) return false;


        for (int i = 0; i < tables1.length; i++) {
            if (tableNames[i].compareTo("EMPLOYEES") != 0) {
                if (!EqualTable.compare(tables1[i], tables2[i])) {
                    sampleLogAssertError(tableNames[i]);
                    return false;
                }

            } else {
                List<Integer> escapeIndex = new ArrayList<Integer>(Arrays.asList(4, 6));
                if (!EqualTable.compareWithEscapeListInd(tables1[i], tables2[i], escapeIndex))
                    return false;

            }
        }
        return true;
    }

    public static void escapeConfserverPropertyValues() {

    }


    private static void sampleLogAssertError(String tableName) {

        System.out.println("Error in equal in " + tableName);

    }

    public static String[] getOrdinaryTables(String[] tables, String confserver) {


        String[] ordinaryTables = new String[tables.length - 1];

//        if(Arrays.asList(tables).contains(confserver)){
//
//        }

        return tables;
    }

}
