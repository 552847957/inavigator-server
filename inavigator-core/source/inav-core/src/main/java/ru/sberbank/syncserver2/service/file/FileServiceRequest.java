package ru.sberbank.syncserver2.service.file;

import javax.servlet.http.HttpServletRequest;

import ru.sberbank.syncserver2.util.HttpRequestUtils;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 27.02.2014
 * Time: 1:46:16
 * To change this template use File | Settings | File Templates.
 */
public class FileServiceRequest {
    public interface COMMANDS {
        int UNKNOWN = 0;
        int LIST = 1;
        int DATA = 2;
        int PREVIEW = 3;
    }

    private int command;
    private String app;
    private String id;
    private int chunkIndex;
    private String userEmail;
    private String userIpAddress;
    private String deviceId;
    private boolean skipPreview;
    private String name;

    public FileServiceRequest(HttpServletRequest request) {
    	setUserEmail(HttpRequestUtils.getUsernameFromRequest(request));
    	setUserIpAddress(HttpRequestUtils.getClientIpAddr(request));

        String cmd = request.getParameter("command");
        if("list".equalsIgnoreCase(cmd)){
            command = COMMANDS.LIST;
        } else if("data".equalsIgnoreCase(cmd)){
            command = COMMANDS.DATA;
        } else if("preview".equalsIgnoreCase(cmd)){
            command = COMMANDS.PREVIEW;
        } else {
            command = COMMANDS.UNKNOWN;
        }
        this.app = request.getParameter("app");
        this.id = request.getParameter("id");
        if(id==null || "".equalsIgnoreCase(id.trim())){
            this.id = request.getParameter("reportId");
        }
        try {
            this.chunkIndex = Integer.parseInt(request.getParameter("chunkIndex"));
        } catch (Exception e) {
            this.chunkIndex = 0;
        }

        String preview = request.getParameter("preview");
        if("false".equalsIgnoreCase(preview)) {
            this.skipPreview = true;
        }

        this.deviceId = request.getParameter("deviceId");
        this.name = request.getParameter("name");
    }


    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isSkipPreview() {
        return skipPreview;
    }

    public void setSkipPreview(boolean skipPreview) {
        this.skipPreview = skipPreview;
    }

    @Override
    public String toString() {
        return "FileServiceRequest{" +
                "command=" + command +
                ", app='" + app + '\'' +
                ", id='" + id + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", userEmail='" + userEmail + '\'' +
                ", userIpAddress='" + userIpAddress + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", preview='" + !skipPreview + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    /**
	 * @return the userEmail
	 */
	public String getUserEmail() {
		return userEmail;
	}

	/**
	 * @param userEmail the userEmail to set
	 */
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserIpAddress() {
		return userIpAddress;
	}

	public void setUserIpAddress(String userIpAddress) {
		this.userIpAddress = userIpAddress;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
