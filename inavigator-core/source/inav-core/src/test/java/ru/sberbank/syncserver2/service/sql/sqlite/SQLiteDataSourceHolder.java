package ru.sberbank.syncserver2.service.sql.sqlite;

import org.apache.commons.dbcp.BasicDataSource;

public class SQLiteDataSourceHolder {

    private BasicDataSource incomeDataSource;
    private BasicDataSource outcomeDataSource;

    public SQLiteDataSourceHolder() {
    }

    public BasicDataSource getIncomeDataSource() {
        return incomeDataSource;
    }

    public void setIncomeDataSource(BasicDataSource incomeDataSource) {
        this.incomeDataSource = incomeDataSource;
    }

    public BasicDataSource getOutcomeDataSource() {
        return outcomeDataSource;
    }

    public void setOutcomeDataSource(BasicDataSource outcomeDataSource) {
        this.outcomeDataSource = outcomeDataSource;
    }

}
