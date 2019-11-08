package ru.sberbank.syncserver2.mybatis.typehandler;

import com.thoughtworks.xstream.XStream;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import ru.sberbank.syncserver2.mybatis.domain.QlikViewDBError;
import ru.sberbank.syncserver2.xstream.qlikview.QlikViewDBErrorConverter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.*;

public class SQLXMLToQlikViewDBErrorTypeHandler implements TypeHandler<QlikViewDBError> {

    private XStream xs;
    private QlikViewDBErrorConverter converter;

    public SQLXMLToQlikViewDBErrorTypeHandler() {
        xs = new XStream();
        converter = new QlikViewDBErrorConverter();
        xs.registerConverter(converter);
        xs.alias("rs", QlikViewDBError.class);
    }


    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, QlikViewDBError qlikViewDBError, JdbcType jdbcType) throws SQLException {
        if (qlikViewDBError != null) {
            preparedStatement.setSQLXML(i, new SQLXML() {

                @Override
                public void free() throws SQLException {

                }

                @Override
                public InputStream getBinaryStream() throws SQLException {
                    return null;
                }

                @Override
                public OutputStream setBinaryStream() throws SQLException {
                    return null;
                }

                @Override
                public Reader getCharacterStream() throws SQLException {
                    return null;
                }

                @Override
                public Writer setCharacterStream() throws SQLException {
                    return null;
                }

                @Override
                public String getString() throws SQLException {
                    return null;
                }

                @Override
                public void setString(String s) throws SQLException {

                }

                @Override
                public <T extends Source> T getSource(Class<T> aClass) throws SQLException {
                    return null;
                }

                @Override
                public <T extends Result> T setResult(Class<T> aClass) throws SQLException {
                    return null;
                }
            });
        } else {
            preparedStatement.setNull(i, java.sql.Types.SQLXML);
        }
    }

    @Override
    public QlikViewDBError getResult(ResultSet resultSet, String s) throws SQLException {
        return null;
    }

    @Override
    public QlikViewDBError getResult(ResultSet resultSet, int i) throws SQLException {
        return null;
    }

    @Override
    public QlikViewDBError getResult(CallableStatement callableStatement, int i) throws SQLException {
        Reader r = callableStatement.getNCharacterStream(i);
        return (QlikViewDBError) xs.fromXML(r);
    }

}
