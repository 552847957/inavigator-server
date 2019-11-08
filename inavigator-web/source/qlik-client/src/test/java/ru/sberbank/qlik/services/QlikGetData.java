package ru.sberbank.qlik.services;

import org.junit.Assert;
import org.junit.Test;
import ru.sberbank.qlik.view.ObjectData;
import ru.sberbank.qlik.view.QlikViewClient;
import ru.sberbank.qlik.view.QlikViewClientRequest;
import ru.sberbank.qlik.view.Response;

import java.util.ArrayList;

import static ru.sberbank.qlik.view.QlikViewClientTest.*;

public class QlikGetData {

    @Test
    public void checkGetData() {
        ArrayList<String> objectIds = new ArrayList<String>();

////        //// Доходы по продуктам Трайба
        objectIds.add("TX3826");//Заголовок
        objectIds.add("CH1039");//График
        objectIds.add("CH1040");//Фактический показатель
////
////        //// Доля рынка кредитования ПК и КК
//        objectIds.add("TX3829");
//        objectIds.add("CH1184");
//        objectIds.add("CH674");
////
////
////        ////Средние остатки привлечения ФЛ
//        objectIds.add("CH1041");//Доля обращений по продуктам трайба
//        objectIds.add("CH1047"); // Технологическая трансформация, Продуктовое развитие в Digital, Кредитный потенциал
//        objectIds.add("CH1162"); // Прибыль Сетелем
//
  //      objectIds.add("CH1166");// Лист слева
        //objectIds.add("CH713");// Стримы таблица

        QlikViewClientRequest qlikViewClientRequest = new QlikViewClientRequest()
                .setDocumentUri(documentUri2)
                .setUser(USER)
                .setPassword(PASSWORD)
                .setObjectIds(objectIds)
                .setDeleteCvs(false)
                .setQuit(true);
        Response data2 = QlikViewClient.getObjectsData(qlikViewClientRequest);
//        Response data2 = QlikViewClient.getObjectsData("C:\\Users\\sbt-biryukov-su\\Desktop\\1.qvw", USER, PASSWORD, objectIds, true, false);
        for (ObjectData objectData : data2.getData()) {
            Assert.assertFalse(objectData.getId(), objectData.isError());
        }
        System.out.println("Finish");
    }
}
