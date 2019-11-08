package ru.sbt.utils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by akv on 18.11.2014.
 */
public class ControlValues {

    static final List<String> valuesEmploees = new ArrayList<String>(Arrays.asList(
            "666",
            "joker@mail.com",
            "joker",
            "1910-01-01 01:10:01.01",
            "joker",
            "1910-01-01 01:10:01.01"));

    static final List<String> valuesSyncConfig = new ArrayList<String>(Arrays.asList(
            "IS_TEST_KEY_FOR_SYNC_CONFIG",
            "IS",
            "TEST_DESC"));
    static final List<String> valuesPropertyValues = new ArrayList<String>(Arrays.asList(
            "10", "1.1.1.1", "onlineService", "test_note"));

    static final  List<String> valuesConfserverVersions = new ArrayList<String>(Arrays.asList(
            "10", "1.1.1.1"
    ));


    public static List<String> getValuesByTable(String tableName) {//TableEnum tableEnum){

        switch (TableEnum.valueOf(tableName.toUpperCase())) {
            case EMPLOYEES: {
                return valuesEmploees;
            }


            case SYNC_CONFIG: {
                return valuesSyncConfig;
            }
            case CONFSERVER_PROPERTY_VALUES: {
                return valuesPropertyValues;
            }

            case CONFSERVER_VERSIONS:{
                return valuesConfserverVersions;

            }


        }

        return valuesEmploees;

    }

}
