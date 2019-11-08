package ru.sberbank.syncserver2.service.sql.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementSetter;



/**
 * @author Sergey Erin
 *
 */
public class StatementSetterImpl implements PreparedStatementSetter {

    private final OnlineRequest request;
    private Map<FieldType, Integer> sqlTypeByFieldType;

    public StatementSetterImpl(OnlineRequest request, Map<FieldType, Integer> sqlTypeByFieldType) {
        this.request = request;
        this.sqlTypeByFieldType = sqlTypeByFieldType;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        if (request.getArguments() != null && request.getArguments().getArgument() != null) {
            List<OnlineRequest.Arguments.Argument> arguments = request.getArguments().getArgument();
            for (OnlineRequest.Arguments.Argument argument : arguments) {
                Integer sqlType = getSqlTypeByFieldType(argument.getType());
                if (sqlType != null) {
                    preparedStatement.setObject(argument.getIndex(), argument.getValue(), sqlType);
                } else {
                    preparedStatement.setObject(argument.getIndex(), argument.getValue());
                }
            }
        }
    }

    private Integer getSqlTypeByFieldType(FieldType fieldType) {
        Integer sqlType = sqlTypeByFieldType.get(fieldType);
        if (sqlType == null) {
            SwitchingDataSourceDAO.log.warn("Can't convert '" + fieldType + "' to java.sql.Types");
        }

        return sqlType;
    }
}