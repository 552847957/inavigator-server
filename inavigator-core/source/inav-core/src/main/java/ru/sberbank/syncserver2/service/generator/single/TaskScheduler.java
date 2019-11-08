package ru.sberbank.syncserver2.service.generator.single;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import ru.sberbank.syncserver2.gui.util.SQLHelper;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.generator.DataSourceHelper;
import ru.sberbank.syncserver2.service.generator.single.data.ETLAction;

import javax.sql.DataSource;

public class TaskScheduler {

    /**
     * See OPU and Balance data.
     * They are generated as a single file, but have separate actual dates.
     */
    private static final int MAX_COUNT_OF_ACTUAL_DATES = 2;

    private static final Logger log = Logger.getLogger(TaskScheduler.class);

    private ConfigLoader configLoader;
    private String actualDateDb;

    private ConcurrentHashMap jdbcTemplates = new ConcurrentHashMap();

    public TaskScheduler(ConfigLoader configLoader) {
        this.configLoader = configLoader;
        this.actualDateDb = configLoader.getSyncConfigProperty("ALPHA_SOURCE_DB");
    }

    /**
     * @param action
     * @return
     */
    public List<Timestamp> getDataActualTimes(ETLAction action) {
        //1. Extracting params
        String jndi = action.getJndi();
        final String patternName = (action.getPatternName()==null ? null: action.getPatternName().toLowerCase());
        String sql = "exec "+actualDateDb + "..SP_IPAD_GET_ACTUAL_DATE '"+patternName+"'";

        //2. Get actual dates
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            DataSource dataSource = DataSourceHelper.getOrCreateDataSource(jndi);
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            List<Timestamp> actualDates = new ArrayList<Timestamp>(2);
            if(rs.next()){
                Timestamp date1 = rs.getTimestamp(1);
                Timestamp date2 = rs.getTimestamp(2);
                actualDates.add(date1);
                actualDates.add(date2);
            }
            log.info("Task [dataFileName: '" + action.getDataFileName() + "'] actual date(s) in MIS time are" +
                    " " + actualDates);
            return actualDates;
        } catch (Exception e) {
        	// TODO: Сюда обязательно надо добавить уведомление, т.к. именно в этом месте при переключении между finik1/finik2
        	// В случае если у пользователя отвалились права на БД, не сравняться даты и молча не произойдет генерация БД.
        	// надо придумать как удобно и красиво получить тут ссылку на DatabaseNotificationLogger
        	// А еще тут нужен tagLogger
        	
            log.info("Task [dataFileName: '" + action.getDataFileName() + "'] check auto run failed with error" + e.getMessage());
            e.printStackTrace();
        } finally {
            SQLHelper.closeResultSet(rs);
            SQLHelper.closeStatement(statement);
            SQLHelper.closeConnection(connection);
        }
        return null;
    }

    public void setJobRefreshTimes(ETLAction action, List<Timestamp> lastRefreshTimes) {
        //1. Composing sql statement
        String patternName = action.getPatternName();
        patternName = patternName.toLowerCase();
        if (lastRefreshTimes == null) {
            return;
        }

        StringBuilder sql = new StringBuilder("exec SP_IPAD_SET_LAST_REFRESH_TIME ?");
        for (int i = 0; i < lastRefreshTimes.size(); i++) {
            sql.append(",?");
        }

        Object[] values = new Object[lastRefreshTimes.size() + 1];
        values[0] = patternName;
        System.arraycopy(lastRefreshTimes.toArray(), 0, values, 1, lastRefreshTimes.size());
        configLoader.executePattern(sql.toString(), values);

        log.info("Task [dataFileName: '" + action.getDataFileName() + "'] saved actual dates for last generation " + lastRefreshTimes);
    }

    public List<Timestamp> getLastJobDataTimes(ETLAction action) {
        final String sql = "exec SP_IPAD_GET_LAST_REFRESH_TIME ?";
        String patternName = action.getPatternName();
        patternName = patternName.toLowerCase();
        List<Timestamp> refreshTimes = configLoader.getValue(sql, new TimestampListExtractor(MAX_COUNT_OF_ACTUAL_DATES), patternName);
        log.info("Task [dataFileName: '" + action.getDataFileName() + "'] actual dates for last generation " + refreshTimes);
        return refreshTimes;
    }

    protected static class TimestampListExtractor implements ResultSetExtractor<List<Timestamp>> {

        private int number;

        public TimestampListExtractor(int number) {
            this.number = number;
        }

        @Override
        public List<Timestamp> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                List<Timestamp> result = new ArrayList<Timestamp>(number);
                for (int i = 1; i <= number; i++) {
                    result.add(resultSet.getTimestamp(i));
                }
                return result;
            }

            //log.warn("No rows returned");
            return null;
        }
    }

}
