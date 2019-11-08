package ru.sberbank.qlik.view;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

// TODO Для тестирования нужен клиент QlikView и сервер QlikView или документ на локальной машине
public class QlikViewClientTest {


    public static final String documentUri = "qvp://10.116.205.60/Рыночная доля.qvw";
    public static String documentUri2 = "qvp://nlb-user-bi-psi/retail_hq/арм р new_29/qvdevt/appstier4/arm_r_new_осрб.qvw";
    public static final String USER = "ALPHA/sbt-biryukov-su";
    public static final String PASSWORD = "";
/*

    @Ignore
    @Test
    public void testOurServer(){
        ArrayList<String> objectIds = new ArrayList<String>();
        objectIds.add("CH45");
        objectIds.add("CH44");
        objectIds.add("CH60");
        QlikViewClientRequest qlikViewClientRequest = new QlikViewClientRequest()
                .setDocumentUri(documentUri)
                .setUser(USER)
                .setPassword(PASSWORD)
                .setObjectIds(objectIds)
                .setDeleteCvs(true)
                .setQuit(true);
        Response objectsData = QlikViewClient.getObjectsData(qlikViewClientRequest);
        int size = objectsData.getData().size();
    }

    @Ignore
    @Test
    public void testOtherServer(){
        ArrayList<String> objectIds = new ArrayList<String>();
        objectIds.add("CH1184");
        objectIds.add("CH1039");
        QlikViewClientRequest qlikViewClientRequest = new QlikViewClientRequest()
                .setDocumentUri(documentUri2)
                .setUser(USER)
                .setPassword(PASSWORD)
                .setObjectIds(objectIds)
                .setDeleteCvs(true)
                .setQuit(true);
        Response objectsData = QlikViewClient.getObjectsData(qlikViewClientRequest);
        int size = objectsData.getData().size();
    }
*/
}
