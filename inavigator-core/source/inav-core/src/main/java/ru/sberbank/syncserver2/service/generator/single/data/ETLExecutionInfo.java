package ru.sberbank.syncserver2.service.generator.single.data;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 08.04.14.
 */
public class ETLExecutionInfo {
    private ETLAction action;
    private List<Timestamp> actualTimes;
    private Long lastStartTime;

    public ETLExecutionInfo(ETLAction action, List<Timestamp> actualTimes) {
        this.action = action;
        this.actualTimes = actualTimes;
    }

    public ETLExecutionInfo(ETLAction action, Long lastStartTime) {
        this.action = action;
        this.lastStartTime = lastStartTime;
    }

    public ETLAction getAction() {
        return action;
    }

    public void setAction(ETLAction action) {
        this.action = action;
    }

    public List<Timestamp> getActualTimes() {
        return actualTimes;
    }

    public void setActualTimes(List<Timestamp> actualTimes) {
        this.actualTimes = actualTimes;
    }

    public Long getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(Long lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    @Override
    public String toString() {
        return ru.sberbank.syncserver2.util.FormatHelper.stringConcatenator("ETLExecutionInfo{",
                "action=", action,
                ", actualTimes=",  actualTimes,
                ", lastStartTime=",  lastStartTime,
                '}');
    }
}
