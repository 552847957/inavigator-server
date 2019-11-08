package ru.sberbank.syncserver2.service.file;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.file.cache.FileCache;
import ru.sberbank.syncserver2.service.file.cache.data.FileCacheConstants;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.security.SecurityService;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;
import ru.sberbank.syncserver2.util.XMLHelper;

/**
 * Created by sbt-kozhinsky-lb on 27.02.14.
 */
public class FileService extends AbstractService implements PublicService {
    private FileCache fileCache;
    private SecurityService securityService;

    public FileCache getFileCache() {
        return fileCache;
    }

    public void setFileCache(FileCache fileCache) {
        this.fileCache = fileCache;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public void request(HttpServletRequest request, HttpServletResponse response) {
        //1. Parsing request
        FileServiceRequest fsRequest = new FileServiceRequest(request);

        //2. Processing request
        //2.1 Checking authorization rights
        ExecutionTimeProfiler.start("FileService.request.permissions");
        String securityError = null;
        if (securityService != null) {
            if(fsRequest.getCommand()==FileServiceRequest.COMMANDS.LIST){
                ExecutionTimeProfiler.start("isAllowedToUseApp");
                boolean allowedToUseApp = securityService.isAllowedToUseApp(fsRequest.getApp(), fsRequest.getUserEmail(), fsRequest.getDeviceId());
                ExecutionTimeProfiler.finish("isAllowedToUseApp");
                if(!allowedToUseApp){
                    logUserEvent(LogEventType.FILE_LIST_START, fsRequest.getUserEmail(), fsRequest.getUserIpAddress(), "security error: not allowed to use app", fsRequest.getApp());
                    securityError = FileCache.format(FileCacheConstants.NOT_ALLOWED_TO_USE_APP, fsRequest.getApp());
                }
            } else if(fsRequest.getCommand()==FileServiceRequest.COMMANDS.DATA){
                ExecutionTimeProfiler.start("isAllowedToDownloadFile");
                boolean allowedToDownloadFile = securityService.isAllowedToDownloadFile(fsRequest.getApp(), fsRequest.getId(), fsRequest.getUserEmail(), fsRequest.getDeviceId());
                ExecutionTimeProfiler.finish("isAllowedToDownloadFile");
                if(!allowedToDownloadFile){
                    logUserEvent(LogEventType.FILE_LIST_START, fsRequest.getUserEmail(), fsRequest.getUserIpAddress(), "security error: not allowed to download file", fsRequest.getId());
                    securityError = FileCache.format(FileCacheConstants.NOT_ALLOWED_TO_LOAD_FILE, fsRequest.getId(), fsRequest.getApp());
                }
            } else if(fsRequest.getCommand()==FileServiceRequest.COMMANDS.PREVIEW){
                ExecutionTimeProfiler.start("isAllowedToDownloadFile");
                boolean allowedToDownloadFile = securityService.isAllowedToDownloadFile(fsRequest.getApp(), fsRequest.getId(), fsRequest.getUserEmail(), fsRequest.getDeviceId());
                ExecutionTimeProfiler.finish("isAllowedToDownloadFile");
                if(!allowedToDownloadFile){
                    logUserEvent(LogEventType.FILE_LIST_START, fsRequest.getUserEmail(), fsRequest.getUserIpAddress(), "security error: not allowed to download file", fsRequest.getId());
                    securityError = FileCache.format(FileCacheConstants.NO_PREVIEW, fsRequest.getId(), fsRequest.getApp());
                }
            }
        }
        ExecutionTimeProfiler.finish("FileService.request.permissions");

        //2.2 Getting data
        FileServiceResponse fsResponse = null;
        if(securityError==null){
            ExecutionTimeProfiler.start("FileService.request.processRequest");
            fsResponse = fileCache.processRequest(fsRequest);
            ExecutionTimeProfiler.finish("FileService.request.processRequest");
        }

        //3. Composing reply
        byte[] data;
        ServletOutputStream output = null;
        try {
            ExecutionTimeProfiler.start("FileService.request.sending.output");
            if(securityError!=null){
                response.setContentType("text/xml");
                response.addHeader("Content-Disposition"
                        , "attachment; reportType=" + fsRequest.getId() );
                data = FileCache.getUTF8Bytes(securityError);
            } else if (fsResponse.getCommand() == FileServiceRequest.COMMANDS.LIST) {
                response.setContentType("text/xml");
                response.addHeader("Content-Disposition"
                        , "attachment; reportType=" + fsRequest.getId() );
                FileInfoList result = fsResponse.getList();
                //System.out.println("LISTING "+result);
                String xml = XMLHelper.writeXMLToString(result, true, FileInfoList.class, FileInfo.class);
                data = FileCache.getUTF8Bytes(xml);
            } else if (fsResponse.getCommand() == FileServiceRequest.COMMANDS.PREVIEW) {
                if (fsResponse.isError()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    data = fsResponse.getData();
                } else {
                    data = fsResponse.getData();
                    if (data == null) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        securityError = FileCache.format(FileCacheConstants.NO_PREVIEW, fsRequest.getId(), fsRequest.getApp());
                        data = FileCache.getUTF8Bytes(securityError);
                    } else {
                        response.setContentType("image/png");
                        response.addHeader("Content-Disposition"
                                , "attachment; reportType=" + fsRequest.getId());
                    }
                }
            } else {
                response.setContentType("application/octet-stream");
                response.addHeader("Content-Disposition"
                        , "attachment; chunkMeta=" + fsResponse.getTitle());
                data = fsResponse.getData();
            }
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
            ExecutionTimeProfiler.finish("FileService.request.sending.output");
        }

    }

    @Override
    protected void doStop() {
        logServiceMessage(LogEventType.SERV_STOP, "stopping service");
        logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    protected void waitUntilStopped() {
    }

    /*
    @Override
    protected void doStart() {
    	log(LogEventType.SERV_START, "start", "Starting service");
    	if (securityServiceBeanCode != null) {
    		ServiceContainer securityContainer = getServiceContainer().getServiceManager().findServiceByBeanCode(securityServiceBeanCode);
    		if (securityContainer != null) {
    			securityService = (SecurityService) securityContainer.getService();
    		} else {
    			getDbLogger().log(LogMsgComposer.composeInternalServiceLogMsg(LogEventType.ERROR, "cannot find security bean", "cannot find security bean for " + securityServiceBeanCode));
    		}

    	}
    	log(LogEventType.SERV_START, "start", "Started service");
    } */
}
