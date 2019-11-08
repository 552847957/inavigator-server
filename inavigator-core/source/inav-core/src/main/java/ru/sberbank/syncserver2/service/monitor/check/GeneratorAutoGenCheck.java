package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.generator.single.pub.GetAutoGenStatusesRequest;
import ru.sberbank.syncserver2.service.generator.single.pub.GetAutoGenStatusesResponse;
import ru.sberbank.syncserver2.service.generator.single.pub.GetAutoGenStatusesResponse.AutoGenStatus;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class GeneratorAutoGenCheck extends AbstractCheckAction {

    private String urls;
    private String waitingIntervalMinuties;

    private String urlSuffix = "/public/request.do";


    public String getWaitingIntervalMinuties() {
        return waitingIntervalMinuties;
    }

    public void setWaitingIntervalMinuties(String waitingIntervalMinuties) {
        this.waitingIntervalMinuties = waitingIntervalMinuties;
    }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public String getUrlSuffix() {
        return urlSuffix;
    }

    public void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
    }

    /**
     * Отправить XML запрос и получить XML ответ
     *
     * @param urlString
     * @param data
     * @param classes
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws JAXBException
     * @throws IOException
     */
    private Object readXMLFromHost(String urlString, Object data, Class[] classes) throws NoSuchAlgorithmException, KeyManagementException, JAXBException, IOException {
        URL url = new URL(urlString);
        Object result = null;
        HttpURLConnection httpsConnection = (HttpURLConnection) url.openConnection();
        httpsConnection.setDoInput(true);
        httpsConnection.setDoOutput(true);
        httpsConnection.setRequestMethod("POST");
        httpsConnection.setRequestProperty("Content-type", "text/xml");
        XMLHelper.writeXML(httpsConnection.getOutputStream(), data, true, classes);
        try {
            result = XMLHelper.readXML(httpsConnection.getInputStream(), classes);
        } finally {
            httpsConnection.getInputStream().close();
        }
        return result;
    }


    @Override
    protected List<CheckResult> doCheck() {
        List<CheckResult> results = new ArrayList<CheckResult>();

        // получаем интервал ожидания завершения генерации после завершения события генерации
        int waitingIntervalMinutiesInt = Integer.valueOf(waitingIntervalMinuties);

        // список url-ов генерации
        String[] urlsArray = urls.split(";");

        // пробегаем по каждой ссылке
        for (String url : urlsArray) {

            GetAutoGenStatusesResponse response = null;
            try {
                // Пробуем отправить запрос к генератору
                response = (GetAutoGenStatusesResponse) readXMLFromHost(url + urlSuffix, new GetAutoGenStatusesRequest(), new Class[]{GetAutoGenStatusesRequest.class, GetAutoGenStatusesResponse.class});
                results.add(new CheckResult("GEN_REQUEST_[" + url + "]", true, LOCAL_HOST_NAME + " says: generator service " + url + urlSuffix + "  request succesfully."));
            } catch (Exception ex) {
                results.add(new CheckResult("GEN_REQUEST_[" + url + "]", false, LOCAL_HOST_NAME + " says: generator service " + url + urlSuffix + "  request error."));
                ex.printStackTrace();
            }


            // Если ответ получен успешно и текущий узел генератора активный, то анализируем ответ
            if (response != null && response.isActive()) {

                for (AutoGenStatus status : response.getStatuses()) {

                    // если запрос для данного файла был неуспешен добавляем оповещение
                    if (status.isMisRequestError()) {
                        results.add(new CheckResult(status.getFileName(), false, LOCAL_HOST_NAME + " says: File " + status.getFileName() + " not generated because requets to mis db error (request stored procedure [SP_IPAD_GET_ACTUAL_DATE])"));
                        continue;
                    }

                    // определяем текущее время
                    long currentTime = System.currentTimeMillis();

                    // Флаг для детектирования просроченной генерации
                    boolean genOverdueDetected = false;

                    // поиск максимальной из всех меток в базе MIS
                    long maximumMis = -1;

                    boolean hasMisNotNullStamp = false;

                    // Пробегаем по всем временным меткам и сравниаем mis с генератором
                    for (int i = 0; i < status.getMisDbTimeStamps().size(); i++) {
                        long gen = 0;
                        long mis = 0;

                        if (status.getMisDbTimeStamps().get(i) != null)
                            mis = status.getMisDbTimeStamps().get(i);
                        if ((i <= status.getGeneratorDbTimeStamp().size() - 1) && (status.getGeneratorDbTimeStamp().get(i) != null))
                            gen = Long.valueOf(status.getGeneratorDbTimeStamp().get(i));

                        // если дата mis > даты в генераторе, то определяем, что генерация не запустилась
                        if (mis > gen)
                            genOverdueDetected = true;
                        // Определяем максимую из меток
                        if (mis > maximumMis)
                            maximumMis = mis;

                        // флаг, что нашли метку mis не нулевую
                        if (mis > 0)
                            hasMisNotNullStamp = true;
                    }

                    // если в списке MIS меток не нашли ни одной значимой - это ошибка ( процедура работает некорректно)
                    if (!hasMisNotNullStamp)
                        results.add(new CheckResult(status.getFileName(), false, LOCAL_HOST_NAME + " says: All timestamps for file " + status.getFileName() + " are null. (request stored procedure [SP_IPAD_GET_ACTUAL_DATE])"));
                        // Если определено, что дата вгенераторе отличается от MIS и дата маскимальной метки от текущего момента произошла ранее чем waitingIntervalMinutiesInt минут, то
                        // формируем уведомление о том, что генерация не удаласб=ь
                    else if (genOverdueDetected && ((currentTime - maximumMis) > (waitingIntervalMinutiesInt * 60 * 1000)))
                        results.add(new CheckResult(status.getFileName(), false, LOCAL_HOST_NAME + " says: File " + status.getFileName() + " not generated in " + waitingIntervalMinuties + " minutes after generation event begins(" + status.getMisDbReadaleDates() + ")."));
                    else
                        results.add(new CheckResult(status.getFileName(), true, LOCAL_HOST_NAME + " says: File " + status.getFileName() + " was succesfully generated."));
                }
            }
        }

        return results;

    }

    @Override
    public String getDescription() {
        return "Чекер для проверки автозапуска генераций";
    }

}
