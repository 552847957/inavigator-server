package ru.sberbank.syncserver2.service.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 29.02.2012
 * Time: 14:58:09
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "error",namespace = "")
public class ResponseError {
    private String code;
    private String description;


    public ResponseError() {
    }

    public ResponseError(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @XmlElement(name="code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlElement(name="description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
