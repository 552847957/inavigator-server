package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.core.ResponseError;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;
import ru.sberbank.syncserver2.util.FormatHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by sbt-kozhinsky-lb on 03.03.14.
 */
public class DataPowerService extends BackgroundService implements SQLService {
    private Logger traceOnlineSql = Logger.getLogger("trace.syncserver.online.sql");
    private String overrideProvider;// = "MSSQL";
    private String overrideService1;// = "finik1-new";
    private String overrideService2;// = "finik2-new";
    private String dataPowerURL1;// = "http://10.21.7.238:4004";
    private String dataPowerURL2;// = "http://10.21.7.238:4004";
    private AtomicLong counter = new AtomicLong(0);
    private String conversion;

    public String getDataPowerURL1() {
        return dataPowerURL1;
    }

    public void setDataPowerURL1(String dataPowerURL1) {
        this.dataPowerURL1 = dataPowerURL1;
    }

    public String getDataPowerURL2() {
        return dataPowerURL2;
    }

    public void setDataPowerURL2(String dataPowerURL2) {
        this.dataPowerURL2 = dataPowerURL2;
    }

    public String getOverrideProvider() {
        return overrideProvider;
    }

    public void setOverrideProvider(String overrideProvider) {
        this.overrideProvider = overrideProvider;
    }

    public String getOverrideService1() {
        return overrideService1;
    }

    public void setOverrideService1(String overrideService1) {
        this.overrideService1 = overrideService1;
    }

    public String getOverrideService2() {
        return overrideService2;
    }

    public void setOverrideService2(String overrideService2) {
        this.overrideService2 = overrideService2;
    }

    public String getConversion() {
        return conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion;
    }

    public DataResponse request(OnlineRequest request) {

        if (traceOnlineSql.isTraceEnabled())
            traceOnlineSql.trace(FormatHelper.stringConcatenator("DataPowerService.request: ", request));

        //1. Count request id
        long requestId = counter.addAndGet(1);

        //2. Executing
        DataResponse response = null;
        //tagLogger.log("sql.do","Start request #"+requestId+" :"+request);

        try {
            response = requestDataFromDataPower(request,requestId);
            if (response == null) {
                response = new DataResponse();
                response.setResult(DataResponse.Result.FAIL);
                response.setError("Empty response received from DataPower");
            }

            if (traceOnlineSql.isTraceEnabled())
                traceOnlineSql.trace(FormatHelper.stringConcatenator("DataPowerService.response: ", response));

        } catch (Exception e) {
        	logger.error(e, e);
            response = new DataResponse();
            response.setResult(DataResponse.Result.FAIL_DP);
            response.setError("Error while processing request to DataPower " + e.getMessage());
        } finally {
            //tagLogger.log("sql.do","Finish request #"+requestId);
            if (traceOnlineSql.isTraceEnabled())
                traceOnlineSql.trace(FormatHelper.stringConcatenator("DataPowerService.finally: ", request));
        }
        return response;
    }

    private DataResponse requestDataFromDataPower(OnlineRequest request, long requestId) throws JAXBException {
        //1. Check if there is any request
        if (request == null) {
            return null;
        }

        //2. Get config
        for (int a=0; a<2; a++) {
            int instance = (int) ((requestId + a) % 2);
            String dataPowerURL = instance==0    ? dataPowerURL1   :dataPowerURL2;
            String overrideService = instance==0 ? overrideService1:overrideService2;

            if(SQLPublicService.CONVERT_TO_NEW_SIGMA.equalsIgnoreCase(conversion) || SQLPublicService.CONVERT_TO_PASSPORT_SIGMA.equalsIgnoreCase(conversion)){
            	if(a==0){
                    if (SQLPublicService.CONVERT_TO_NEW_SIGMA.equalsIgnoreCase(conversion) ){// for iNavigator
                        request.convertToNewSigma();
                    } else if(SQLPublicService.CONVERT_TO_PASSPORT_SIGMA.equalsIgnoreCase(conversion)){ // for Passport
                        request.convertToPassportSigma();
                    }
                    request.setProvider(overrideProvider);
            	}
                request.setAlphaWebHost(overrideService);
            } else {
                request.setService(overrideService);
                request.setProvider(overrideProvider);
            }


            try {
                ExecutionTimeProfiler.start("requestDataFromDataPower");
                URL url = new URL(dataPowerURL);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

                httpConnection.setReadTimeout(3*60*1000); // 3 minutes
                httpConnection.setDoOutput(true);
                httpConnection.setDoInput(true);

                httpConnection.setRequestMethod("POST");
                httpConnection.setRequestProperty("Accept", "text/xml");
                httpConnection.setRequestProperty("Content-type", "text/xml");

                OutputStreamWriter out = new OutputStreamWriter(
                        httpConnection.getOutputStream(), "UTF-8");
                String requestString = XMLHelper.writeXMLToString(request, false, OnlineRequest.class);

                //tagLogger.log("DatPowerService.request",requestString);
                out.write(requestString);
                out.close();
                if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK ||
                		StringUtils.containsIgnoreCase(httpConnection.getHeaderField("X-Backside-Transport"), "FAIL")) {
                    if(a==1){
                        throw new IllegalStateException("Error receiving response from " + dataPowerURL + ": response code is "
                        				+ httpConnection.getResponseCode() + " and X-Backside-Transport header is " + httpConnection.getHeaderField("X-Backside-Transport"));
                    } else {
                        continue;
                    }
                }

                DataResponse response = null;
                try {
                    ExecutionTimeProfiler.start("requestDataFromDataPower.receive");
                    Object o = XMLHelper.readXML(httpConnection.getInputStream(), DataResponse.class, ResponseError.class);
                    if (o instanceof ResponseError) {
                    	if (a==0)
                    		continue;
                        response = new DataResponse();
                        response.setResult(((ResponseError)o).getCode().contains("0x00030003") ? DataResponse.Result.FAIL_DP : DataResponse.Result.FAIL);
                        response.setError("Error in received response from " + dataPowerURL + " : " + ((ResponseError)o).getCode() + " - " + ((ResponseError)o).getDescription());
                    }
                    if (o instanceof DataResponse) {
                        response = (DataResponse) o;
                    }
                } finally {
                    httpConnection.getInputStream().close();
                    ExecutionTimeProfiler.finish("requestDataFromDataPower.receive");
                }
                return response;
            } catch (IOException ex) {
                if(a==1){
                    throw new IllegalStateException("Error sending request to " + dataPowerURL + " : " + ex.getMessage());
                } else {
                    continue;
                }
            } finally {
                ExecutionTimeProfiler.finish("requestDataFromDataPower");
            }
        }
        return null;
    }

    @Override
    protected void doStop() {
    	logServiceMessage(LogEventType.SERV_STOP, "stopping service");
    	logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    protected void doStart() {
    	logServiceMessage(LogEventType.SERV_START, "starting service");
    	logServiceMessage(LogEventType.SERV_START, "started service");
    }

    public static void main(String[] args) {
        //1. Creating online request
        OnlineRequest or = new OnlineRequest();
        or.setStoredProcedure("exec [MIS_PCA_CIB_DYNAMIC_MODEL].[dbo].[p_getBranches_FilterXML]");
        or.setProvider("DATAPOWER");
        or.setService("finik1");

        //2. Runing onlune request
        System.out.println("REQUEST: "+or);
        DataPowerService service = new DataPowerService();
        DataResponse dr = service.request(or);
        System.out.println("RESULT: "+dr);
    }
}
