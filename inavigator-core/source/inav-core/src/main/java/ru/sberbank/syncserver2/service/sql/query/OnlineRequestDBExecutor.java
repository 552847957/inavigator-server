package ru.sberbank.syncserver2.service.sql.query;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Sergey Erin
 *
 */
public class OnlineRequestDBExecutor {

    private static final Logger log = Logger.getLogger(OnlineRequestDBExecutor.class);

    private JdbcTemplate jdbcTemplate;

    private Map<FieldType, Integer> sqlTypeByFieldType;
    private Map<Integer, FieldType> fieldTypeBySqlType;

    /**
     * @param jdbcTemplate
     */
    public OnlineRequestDBExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        initSqlTypes();
    }

    private void initSqlTypes() {
        sqlTypeByFieldType = new HashMap<FieldType, Integer>();
        fieldTypeBySqlType = new HashMap<Integer, FieldType>();

        sqlTypeByFieldType.put(FieldType.DATE, java.sql.Types.TIMESTAMP);
        sqlTypeByFieldType.put(FieldType.NUMBER, java.sql.Types.DECIMAL);
        sqlTypeByFieldType.put(FieldType.STRING, java.sql.Types.VARCHAR);
        sqlTypeByFieldType.put(FieldType.BLOB, java.sql.Types.BLOB);

        fieldTypeBySqlType.put(java.sql.Types.TIMESTAMP, FieldType.DATE);
        fieldTypeBySqlType.put(java.sql.Types.INTEGER, FieldType.NUMBER);
        fieldTypeBySqlType.put(java.sql.Types.VARCHAR, FieldType.STRING);
        fieldTypeBySqlType.put(java.sql.Types.BLOB, FieldType.BLOB);
        fieldTypeBySqlType.put(java.sql.Types.VARBINARY, FieldType.BLOB);
        fieldTypeBySqlType.put(java.sql.Types.DECIMAL, FieldType.NUMBER);
        fieldTypeBySqlType.put(java.sql.Types.NVARCHAR, FieldType.STRING);
        
        //новые типы        
        fieldTypeBySqlType.put(java.sql.Types.BINARY, FieldType.BLOB);
        
        fieldTypeBySqlType.put(java.sql.Types.DATE, FieldType.DATE);
        
        fieldTypeBySqlType.put(java.sql.Types.BIGINT, FieldType.NUMBER);
        fieldTypeBySqlType.put(java.sql.Types.SMALLINT, FieldType.NUMBER);
        fieldTypeBySqlType.put(java.sql.Types.TINYINT, FieldType.NUMBER);
        fieldTypeBySqlType.put(java.sql.Types.NUMERIC, FieldType.NUMBER);
        fieldTypeBySqlType.put(java.sql.Types.FLOAT, FieldType.NUMBER);
        fieldTypeBySqlType.put(java.sql.Types.REAL, FieldType.NUMBER);
        
        fieldTypeBySqlType.put(java.sql.Types.CHAR, FieldType.STRING);
        fieldTypeBySqlType.put(java.sql.Types.NCHAR, FieldType.STRING);
        fieldTypeBySqlType.put(java.sql.Types.BIT, FieldType.STRING);
        fieldTypeBySqlType.put(java.sql.Types.TIME, FieldType.STRING);
        
        
    }

    public DataResponse query(final OnlineRequest request) {
        DataResponse response = new DataResponse();
        Map<String, FieldType> expectedTypes = getExpectedFieldTypes(request);

        try {
            jdbcTemplate.query(request.getStoredProcedure(),
                new StatementSetterImpl(request, sqlTypeByFieldType),
                new ResultSetExtractorImpl(expectedTypes, response, fieldTypeBySqlType));
        } catch(ArrayIndexOutOfBoundsException e) {
            response.setResult(Result.FAIL_DB);
            int numberOfArguments = 0;
            if (request.getArguments() != null && request.getArguments().getArgument() != null) {
                numberOfArguments = request.getArguments().getArgument().size();
            }
            response.setError("Wrong number of arguments: " + numberOfArguments);
        } catch(Exception e) {
            response.setResult(Result.FAIL_DB);
            response.setError(e.getMessage());
            log.error(e, e);
        }

        return response;
    }

    /**
     * @param request
     * @return
     */
    private Map<String, FieldType> getExpectedFieldTypes(OnlineRequest request) {
        Map<String, FieldType> result = new HashMap<String, FieldType>();
        if (request != null && request.getDatasetMetaData() != null && request.getDatasetMetaData().getFields() != null) {
            List<DatasetFieldMetaData> fields = request.getDatasetMetaData().getFields();
            for (DatasetFieldMetaData field : fields) {
                result.put(field.getName(), field.getType());
            }
        }

        return result;
    }

}
