package ru.sberbank.syncserver2.service.file.diff;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.file.cache.FileCache;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.Dataset;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by sbt-kozhinsky-lb on 02.07.14.
 */
public class CacheDiffChecker extends AbstractService implements PublicService {
    private String fileCacheBean;
    private String dataPowerBeanCode;
    private String service = "finik2-new";

    private String sqlList = "select id from [MIS_RUBRICATOR].dbo.[resource] (nolock) a where a.resource_type in (4,8) and exists (select 1 from [MIS_RUBRICATOR].dbo.[report_resource] (nolock) b, [MIS_RUBRICATOR].dbo.[report] (nolock) c where b.report_id = c.id and a.id = b.resource_id)";




	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getFileCacheBean() {
        return fileCacheBean;
    }

    public void setFileCacheBean(String fileCacheBean) {
        this.fileCacheBean = fileCacheBean;
    }

    public String getDataPowerBeanCode() {
        return dataPowerBeanCode;
    }

    public void setDataPowerBeanCode(String dataPowerBeanCode) {
        this.dataPowerBeanCode = dataPowerBeanCode;
    }

    public String getSqlList() {
        return sqlList;
    }

    public void setSqlList(String sqlList) {
        this.sqlList = sqlList;
    }

    public FileDiffList getDifference(String app){
        //1. Getting list of resource from Alpha
        //1.1. Prepare request
        OnlineRequest request = new OnlineRequest();
        request.setStoredProcedure(sqlList);
        request.setProvider("DISPATCHER");
        request.setService(service);

        //1.2. Query datapower
        DataPowerService dataPowerService = (DataPowerService) ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerService.class);
        DataResponse dr = dataPowerService.request(request);
        Dataset ds = dr==null ? null:dr.getDataset();
        List<DatasetRow> rows = ds==null ? Collections.EMPTY_LIST:ds.getRows();
        Set<String> alphaResources = new HashSet<String>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            DatasetRow row =  rows.get(i);
            List<String> fields = row==null ? null:row.getValues();
            String resource = (fields==null || fields.size()!=1) ? null:fields.get(0);
            if(resource!=null){
                alphaResources.add("/"+resource);
            }
        }

        //2. Get list of all files from file cache
        ServiceContainer fileCacheContainer = ServiceManager.getInstance().findServiceByBeanCode(fileCacheBean);
        FileCache fileCache = fileCacheContainer==null ? null:(FileCache) fileCacheContainer.getService();
        FileInfoList fileInfoList = fileCache==null ? new FileInfoList(Collections.EMPTY_LIST):fileCache.getFileList(app);
        List<FileInfo> fileInfos = fileInfoList.getReportStatuses();
        Set<String> sigmaResources = new HashSet<String>(fileInfos.size());
        for (FileInfo fileInfo: fileInfos) {
            sigmaResources.add(fileInfo.getCaption());
        }

        //3. Comparing
        int size = sigmaResources.size()+alphaResources.size();
        List<FileDiff> results = new ArrayList<FileDiff>(size);
        for (String sigmaResource: sigmaResources) {
            FileDiff info = null;
            if (alphaResources.contains(sigmaResource)) {
                info = new FileDiff(sigmaResource, FileDiff.EXISTS_IN_BOTH);
            } else {
                info = new FileDiff(sigmaResource, FileDiff.EXISTS_IN_SIGMA);
            }
            results.add(info);
        }

        for (String alphaResource: alphaResources) {
            if (!sigmaResources.contains(alphaResource)) {
                FileDiff info = new FileDiff(alphaResource, FileDiff.EXISTS_IN_ALPHA);
                results.add(info);
            }
        }

        //4. Compose answer
        return new FileDiffList(results);
    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void waitUntilStopped() {

    }

    @Override
    public void request(HttpServletRequest request, HttpServletResponse response) {
        //1. Get comparison
        FileDiffList diffList = getDifference("rubricator");

        //2. Write comparison to response
        byte[] data;
        ServletOutputStream output = null;
        try {
            response.setContentType("text/xml");
            response.addHeader("Content-Disposition", "attachment; ");
            String xml = XMLHelper.writeXMLToString(diffList, true, FileDiff.class, FileDiffList.class);
            data = FileCache.getUTF8Bytes(xml);
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentLength(data.length);
            output = response.getOutputStream();
            output.write(data);
            output.flush();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        } finally {
            if(output!=null){
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

