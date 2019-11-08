package ru.sberbank.syncserver2.mybatis.domain;

import ru.sberbank.syncserver2.util.FormatHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <rs Load_ID="47">
 * <r Error_ID="483" KPI_ID="16078" ButchNumber="3" BatchStartTime="2018-01-22T18:18:09.740" ErrorNumber="241" ErrorSeverity="16" ErrorMessage="Conversion failed when converting date and/or time from character string." />
 * <r Error_ID="484" KPI_ID="16079" ButchNumber="2" BatchStartTime="2018-01-22T18:18:09.753" ErrorNumber="241" ErrorSeverity="16" ErrorMessage="Conversion failed when converting date and/or time from character string." />
 * <r Error_ID="485" KPI_ID="16080" ButchNumber="2" BatchStartTime="2018-01-22T18:18:09.770" ErrorNumber="241" ErrorSeverity="16" ErrorMessage="Conversion failed when converting date and/or time from character string." />
 * <r Error_ID="486" KPI_ID="16081" ButchNumber="1" BatchStartTime="2018-01-22T18:18:09.783" ErrorNumber="2812" ErrorSeverity="16" ErrorMessage="Could not find stored procedure 'qwe'." />
 * <r Error_ID="487" KPI_ID="16082" ButchNumber="1" BatchStartTime="2018-01-22T18:18:09.790" ErrorNumber="2812" ErrorSeverity="16" ErrorMessage="Could not find stored procedure 'qwe'." />
 * <r Error_ID="488" KPI_ID="16083" ButchNumber="1" BatchStartTime="2018-01-22T18:18:09.803" ErrorNumber="2812" ErrorSeverity="16" ErrorMessage="Could not find stored procedure 'qwe'." />
 * <r Error_ID="489" KPI_ID="16084" ButchNumber="1" BatchStartTime="2018-01-22T18:18:09.813" ErrorNumber="2812" ErrorSeverity="16" ErrorMessage="Could not find stored procedure 'qwe'." />
 * <r Error_ID="490" KPI_ID="16085" ButchNumber="1" BatchStartTime="2018-01-22T18:18:09.823" ErrorNumber="2812" ErrorSeverity="16" ErrorMessage="Could not find stored procedure 'qwe'." />
 * </rs>
 */
public class QlikViewDBError implements Serializable {

    private Long loadID;
    private List<QlikViewDBError> errorList = null;
    private Long errorID;
    private Long kpiID;
    private Long butchNumber;
    private Date batchStartTime;
    private Long errorNumber;
    private Long errorSeverity;
    private String errorMessage;


    public QlikViewDBError() {
    }

    public Long getLoadID() {
        return loadID;
    }

    public void setLoadID(Long loadID) {
        this.loadID = loadID;
    }

    public List<QlikViewDBError> getErrorList() {
        if (errorList == null) {
            errorList = new ArrayList<QlikViewDBError>();
        }
        return errorList;
    }

    public void setErrorList(List<QlikViewDBError> errorList) {
        this.errorList = errorList;
    }

    public Long getErrorID() {
        return errorID;
    }

    public void setErrorID(Long errorID) {
        this.errorID = errorID;
    }

    public Long getKpiID() {
        return kpiID;
    }

    public void setKpiID(Long kpiID) {
        this.kpiID = kpiID;
    }

    public Long getButchNumber() {
        return butchNumber;
    }

    public void setButchNumber(Long butchNumber) {
        this.butchNumber = butchNumber;
    }

    public Date getBatchStartTime() {
        return batchStartTime;
    }

    public void setBatchStartTime(Date batchStartTime) {
        this.batchStartTime = batchStartTime;
    }

    public Long getErrorNumber() {
        return errorNumber;
    }

    public void setErrorNumber(Long errorNumber) {
        this.errorNumber = errorNumber;
    }

    public Long getErrorSeverity() {
        return errorSeverity;
    }

    public void setErrorSeverity(Long errorSeverity) {
        this.errorSeverity = errorSeverity;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean hasError() {
        return getErrorList().size() > 0;
    }
    public String getAllErrors() {
        StringBuilder sb = new StringBuilder();
        for (QlikViewDBError err:
             errorList) {
            sb.append(err.getErrorMessage());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return FormatHelper.stringConcatenator("QlikViewDBError{",
                "loadID=", loadID,
                ", errorList=", errorList,
                ", errorID=", errorID,
                ", kpiID=", kpiID,
                ", butchNumber=", butchNumber,
                ", batchStartTime=", batchStartTime,
                ", errorNumber=", errorNumber,
                ", errorSeverity=", errorSeverity,
                ", errorMessage='", errorMessage, '\'',
                '}');
    }
}
