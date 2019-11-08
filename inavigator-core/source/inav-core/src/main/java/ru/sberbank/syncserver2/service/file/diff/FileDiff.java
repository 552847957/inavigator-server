package ru.sberbank.syncserver2.service.file.diff;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by sbt-kozhinsky-lb on 02.07.14.
 */
@XmlType(name="file-diff",propOrder = {"caption","statusDesc"})
public class FileDiff {
    private String caption;
    private int status;

    public static final int EXISTS_IN_SIGMA  = 1;
    public static final int EXISTS_IN_BOTH   = 2;
    public static final int EXISTS_IN_ALPHA  = 3;

    public FileDiff() {
    }

    public FileDiff(String caption, int status) {
        this.caption = caption;
        this.status = status;
    }

    @XmlAttribute(name = "caption")
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @XmlTransient
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @XmlAttribute(name = "statusDesc")
    public String getStatusDesc() {
        switch (status){
            case EXISTS_IN_ALPHA:return "exists in Alpha only";
            case EXISTS_IN_SIGMA:return "exists in Sigma only";
            case EXISTS_IN_BOTH: return "exists in Alpha and Sigma";
        }
        return "undefined";
    }
    
    public void setStatusDesc(String status) {
        if (status.equals("exists in Alpha only")) this.status = EXISTS_IN_ALPHA;
        else if (status.equals("exists in Sigma only")) this.status = EXISTS_IN_SIGMA;
        else if (status.equals("exists in Alpha and Sigma")) this.status = EXISTS_IN_BOTH; 
    }

    @Override
    public String toString() {
        return "FileDiff{" +
                "caption='" + caption + '\'' +
                ", status=" + status +
                '}';
    }

}
