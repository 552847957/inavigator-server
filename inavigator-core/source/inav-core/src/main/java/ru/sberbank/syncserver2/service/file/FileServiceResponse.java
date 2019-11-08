package ru.sberbank.syncserver2.service.file;

import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 27.02.2014
 * Time: 1:57:26
 * To change this template use File | Settings | File Templates.
 */
public class FileServiceResponse {
    private int command;
    private String title;
    private byte[] data;
    private FileInfoList list;
    private boolean error = false;

    public FileServiceResponse(int command, String title, byte[] data) {
        this.command = command;
        this.title = title;
        this.data = data;
    }

    public FileServiceResponse(String title, byte[] data) {
        this.command = FileServiceRequest.COMMANDS.DATA;
        this.title = title;
        this.data = data;
    }

    public FileServiceResponse(FileInfoList list) {
        this.command = FileServiceRequest.COMMANDS.LIST;
        this.list = list;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public FileInfoList getList() {
        return list;
    }

    public void setList(FileInfoList list) {
        this.list = list;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
