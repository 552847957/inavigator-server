package ru.sberbank.syncserver2.service.core;

import org.mockito.Mockito;
import org.mockito.internal.invocation.InvocationImpl;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static ru.sberbank.syncserver2.service.core.config.MSSQLConfigLoader.*;

public class DataSourceAnswer implements org.mockito.stubbing.Answer {
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method m = ((InvocationImpl) invocation).getMethod();
        if ("getConnection".equals(m.getName())) {
            Connection connection = Mockito.mock(Connection.class, new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Method m = ((InvocationImpl) invocation).getMethod();
                    if ("createStatement".equals(m.getName())) {
                        Statement statment = Mockito.mock(Statement.class, new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                Method m = ((InvocationImpl) invocation).getMethod();
                                if ("executeQuery".equals(m.getName())) {
                                    Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                    if (arguments.length > 0) {
                                        Object firstArgument = arguments[0];
                                        if (firstArgument instanceof String) {
                                            if (MSSQL_QUERY_MACROS.equals(firstArgument)) {
                                                ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                    private int nextCount = 0;

                                                    @Override
                                                    public Object answer(InvocationOnMock invocation) throws Throwable {
                                                        Method m = ((InvocationImpl) invocation).getMethod();
                                                        if ("next".equals(m.getName())) {
                                                            nextCount++;
                                                            if (nextCount < 21) {
                                                                return true;
                                                            } else {
                                                                return false;
                                                            }
                                                        }
                                                        if ("getString".equals(m.getName())) {
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if (arguments.length > 0) {
                                                                Object firstArgument = arguments[0];
                                                                if (new Integer(1).equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "QLIK_SENSE_TARGET1";
                                                                        }
                                                                        case 2: {
                                                                            return "QLIK_SENSE_SERVER_HOST1";
                                                                        }
                                                                        case 3: {
                                                                            return "QLIK_SENSE_SERVER_PORT1";
                                                                        }
                                                                        case 4: {
                                                                            return "QLIK_SENSE_SERVER_CONTEXT1";
                                                                        }
                                                                        case 5: {
                                                                            return "QLIK_SENSE_SERTIFICATE_DIR1";
                                                                        }
                                                                        case 6: {
                                                                            return "QLIK_SENSE_ROOT_SERTIFICATE_FILE_NAME1";
                                                                        }
                                                                        case 7: {
                                                                            return "QLIK_SENSE_CLIENT_SERTIFICATE_FILE_NAME1";
                                                                        }
                                                                        case 8: {
                                                                            return "QLIK_SENSE_CLIENT_KEY_FILE_NAME1";
                                                                        }
                                                                        case 9: {
                                                                            return "QLIK_SENSE_CLIENT_KEY_PASSWORD1";
                                                                        }
                                                                        case 10: {
                                                                            return "QLIK_SENSE_USER_NAME1";
                                                                        }
                                                                        case 11: {
                                                                            return "QLIK_SENSE_USER_DOMAIN1";
                                                                        }
                                                                        case 12: {
                                                                            return "QLIK_SENSE_POOL_MAX_TOTAL1";
                                                                        }
                                                                        case 13: {
                                                                            return "QLIK_SENSE_POOL_MAX_IDLE1";
                                                                        }
                                                                        case 14: {
                                                                            return "QLIK_SENSE_POOL_MIN_IDLE1";
                                                                        }

                                                                        case 15: {
                                                                            return "MSSQL_URL1";
                                                                        }
                                                                        case 16: {
                                                                            return "MSSQL_USER1";
                                                                        }
                                                                        case 17: {
                                                                            return "MSSQL_PASSWORD1";
                                                                        }
                                                                        case 18: {
                                                                            return "DATAPOWER_TARGET1";
                                                                        }
                                                                        case 19: {
                                                                            return "MSSQL_TIMEOUT";
                                                                        }
                                                                        case 20: {
                                                                            return "MSSQL_FORCED_TEMPLATE";
                                                                        }

                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                if (new Integer(2).equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "QLIK_SENSE";
                                                                        }
                                                                        case 2: {
                                                                            return "sbt-csit-011.ca.sbrf.ru";
                                                                        }
                                                                        case 3: {
                                                                            return "4747";
                                                                        }
                                                                        case 4: {
                                                                            return "/app";
                                                                        }
                                                                        case 5: {
                                                                            return "./qlik_sense/sertificate/1";
                                                                        }
                                                                        case 6: {
                                                                            return "root.pem";
                                                                        }
                                                                        case 7: {
                                                                            return "client.pem";
                                                                        }
                                                                        case 8: {
                                                                            return "client_key_8.pem";
                                                                        }
                                                                        case 9: {
                                                                            return "";
                                                                        }
                                                                        case 10: {
                                                                            return "sbt-biryukov-su";
                                                                        }
                                                                        case 11: {
                                                                            return "ALPHA";
                                                                        }
                                                                        case 12: {
                                                                            return "8";
                                                                        }
                                                                        case 13: {
                                                                            return "8";
                                                                        }
                                                                        case 14: {
                                                                            return "0";
                                                                        }

                                                                        case 15: {
                                                                            return "MSSQL_URL1";
                                                                        }
                                                                        case 16: {
                                                                            return "MSSQL_USER1";
                                                                        }
                                                                        case 17: {
                                                                            return "MSSQL_PASSWORD1";
                                                                        }
                                                                        case 18: {
                                                                            return "DATAPOWER_TARGET1";
                                                                        }
                                                                        case 19: {
                                                                            return "MSSQL_TIMEOUT";
                                                                        }
                                                                        case 20: {
                                                                            return "MSSQL_FORCED_TEMPLATE";
                                                                        }

                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }
                                                        return null;
                                                    }
                                                });
                                                return resultSet;
                                            }
                                            if (EXEC_SP_SYNC_LIST_FOLDERS.equals(firstArgument)) {
                                                ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                    private int nextCount = 0;

                                                    @Override
                                                    public Object answer(InvocationOnMock invocation) throws Throwable {
                                                        Method m = ((InvocationImpl) invocation).getMethod();
                                                        if ("next".equals(m.getName())) {
                                                            nextCount++;
                                                            if (nextCount < 2) {
                                                                return true;
                                                            } else {
                                                                return false;
                                                            }
                                                        }
                                                        if ("getInt".equals(m.getName())) {
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if (arguments.length > 0) {
                                                                Object firstArgument = arguments[0];
                                                                if ("START_ORDER".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return 1;
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }
                                                        if ("getString".equals(m.getName())) {
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if (arguments.length > 0) {
                                                                Object firstArgument = arguments[0];
                                                                if ("SYNC_FOLDER_CODE".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "proxy";
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                if ("SYNC_FOLDER_DESC".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "alpha proxy";
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }
                                                        return null;
                                                    }
                                                });
                                                return resultSet;
                                            }
                                        }
                                    }
                                }
                                return null;
                            }
                        });
                        return statment;
                    }
                    if ("prepareStatement".equals(m.getName())) {
                        Object[] arguments = ((InvocationImpl) invocation).getArguments();
                        if (arguments.length > 0) {
                            Object firstArgument = arguments[0];
                            if (firstArgument instanceof String) {
                                if (((String) firstArgument).startsWith("SELECT PROPERTY_VALUE FROM SYNC_CONFIG WHERE PROPERTY_KEY = ")) {

                                    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class, new Answer() {
                                        @Override
                                        public Object answer(InvocationOnMock invocation) throws Throwable {
                                            Method m = ((InvocationImpl) invocation).getMethod();
                                            if ("executeQuery".equals(m.getName())) {
                                                ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                    private int nextCount = 0;

                                                    @Override
                                                    public Object answer(InvocationOnMock invocation) throws Throwable {
                                                        Method m = ((InvocationImpl) invocation).getMethod();
                                                        if ("next".equals(m.getName())) {
                                                            nextCount++;
                                                            if (nextCount < 2) {
                                                                return true;
                                                            } else {
                                                                return false;
                                                            }
                                                        }
                                                        if ("getString".equals(m.getName())) {
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if (arguments.length > 0) {
                                                                Object firstArgument = arguments[0];
                                                                if (new Integer(1).equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "FileLogService";
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        return null;
                                                    }
                                                });
                                                return resultSet;
                                            }
                                            return null;
                                        }
                                    });
                                    return preparedStatement;
                                }
                                if (EXEC_SP_SYNC_LIST_SERVICES.equals(firstArgument)) {

                                    /*
                                    rs.getLong("SYNC_SERVICE_ID"),
                                    rs.getString("BEAN_CODE"),
                                    rs.getString("BEAN_CLASS"),
                                    rs.getString("PARENT_BEAN_CODE"),
                                    rs.getString("PARENT_BEAN_PROPERTY"),
                                    rs.getInt("START_ORDER"),
                                    rs.getString("PUBLIC_SERVLET_PATH"),
                                    rs.getString("BEAN_DESC"));
                                     */

                                    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class, new Answer() {

                                        @Override
                                        public Object answer(InvocationOnMock invocation) throws Throwable {
                                            Method m = ((InvocationImpl) invocation).getMethod();
                                            if ("executeQuery".equals(m.getName())) {

                                                ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                    private int nextCount = 0;

                                                    @Override
                                                    public Object answer(InvocationOnMock invocation) throws Throwable {

                                                        Method m = ((InvocationImpl) invocation).getMethod();
                                                        Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                        if ("next".equals(m.getName())) {
                                                            nextCount++;
                                                            if (nextCount < 5) {
                                                                return true;
                                                            } else {
                                                                return false;
                                                            }
                                                        }
                                                        if ("getLong".equals(m.getName())) {
                                                            if (arguments.length > 0) {
                                                                Object firstArgument = arguments[0];
                                                                if ("SYNC_SERVICE_ID".equals(firstArgument)) {
                                                                    return nextCount;
                                                                }
                                                            }
                                                        }
                                                        if ("getInt".equals(m.getName())) {
                                                            if (arguments.length > 0) {
                                                                Object firstArgument = arguments[0];
                                                                if ("START_ORDER".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return 4;
                                                                        }
                                                                        case 2: {
                                                                            return 3;
                                                                        }
                                                                        case 3: {
                                                                            return 2;
                                                                        }
                                                                        case 4: {
                                                                            return 1;
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                            }
                                                        }
                                                        if ("getString".equals(m.getName())) {
                                                            if (arguments.length > 0) {
                                                                Object firstArgument = arguments[0];
                                                                if ("BEAN_CODE".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "proxyQlikSenseService1";
                                                                        }
                                                                        case 2: {
                                                                            return "proxyMSSQLService1";
                                                                        }
                                                                        case 3: {
                                                                            return "proxyDispatcherService";
                                                                        }
                                                                        case 4: {
                                                                            return "proxySQLPublicService";
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                if ("BEAN_CLASS".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "ru.sberbank.syncserver2.service.sql.QlikSenseService";
                                                                        }
                                                                        case 2: {
                                                                            return "ru.sberbank.syncserver2.service.sql.MSSQLService";
                                                                        }
                                                                        case 3: {
                                                                            return "ru.sberbank.syncserver2.service.sql.SQLDispatcherService";
                                                                        }
                                                                        case 4: {
                                                                            return "ru.sberbank.syncserver2.service.sql.SQLPublicService";
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                                if ("PARENT_BEAN_CODE".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "proxyDispatcherService";
                                                                        }
                                                                        case 2: {
                                                                            return "proxyDispatcherService";
                                                                        }
                                                                        case 3: {
                                                                            return "proxySQLPublicService";
                                                                        }
                                                                        case 4: {
                                                                            return null;
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                                if ("PARENT_BEAN_PROPERTY".equals(firstArgument)) {
                                                                    switch (nextCount) {
                                                                        case 1: {
                                                                            return "subService";
                                                                        }
                                                                        case 2: {
                                                                            return "subService";
                                                                        }
                                                                        case 3: {
                                                                            return "sqlDispatcherService";
                                                                        }
                                                                        case 4: {
                                                                            return null;
                                                                        }
                                                                        default: {
                                                                            break;
                                                                        }
                                                                    }

                                                                }
                                                                if ("PUBLIC_SERVLET_PATH".equals(firstArgument)) {
                                                                    if (nextCount == 4) {
                                                                        return "online.do";
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        return null;
                                                    }

                                                });
                                                return resultSet;
                                            }
                                            return null;
                                        }

                                    });
                                    return preparedStatement;

                                }
                                if (EXEC_SP_SYNC_LIST_PROPERTIES.equals(firstArgument)) {
                                    /*
                                rs.getLong("VALUE_ID"),
                                rs.getString("TEMPLATE_CODE"),
                                rs.getString("VALUE"),
                                rs.getString("TEMPLATE_DESC")
                                     */
                                    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class, new Answer() {

                                        private String argBeanCode;

                                        @Override
                                        public Object answer(InvocationOnMock invocation) throws Throwable {
                                            Method m = ((InvocationImpl) invocation).getMethod();
                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                            if ("setString".equals(m.getName())) {
                                                argBeanCode = (String) arguments[1];
                                            }
                                            if ("executeQuery".equals(m.getName())) {

                                                if ("adminFileLogService".equals(argBeanCode)) {

                                                    ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                        private int nextCount = 0;

                                                        @Override
                                                        public Object answer(InvocationOnMock invocation) throws Throwable {

                                                            Method m = ((InvocationImpl) invocation).getMethod();
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if ("next".equals(m.getName())) {
                                                                nextCount++;
                                                                if (nextCount < 2) {
                                                                    return true;
                                                                } else {
                                                                    return false;
                                                                }
                                                            }
                                                            if ("getLong".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("VALUE_ID".equals(firstArgument)) {
                                                                        return 20 + nextCount;
                                                                    }
                                                                }
                                                            }
                                                            if ("getString".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("TEMPLATE_CODE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "logFileName";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    if ("VALUE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "test/tmp/directory/logs/ServiceManagerTest.log";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                    if ("TEMPLATE_DESC".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "1";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                            return null;
                                                        }

                                                    });
                                                    return resultSet;

                                                }

                                                if ("proxyQlikSenseService1".equals(argBeanCode)) {

                                                    ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                        private int nextCount = 0;

                                                        @Override
                                                        public Object answer(InvocationOnMock invocation) throws Throwable {

                                                            Method m = ((InvocationImpl) invocation).getMethod();
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if ("next".equals(m.getName())) {
                                                                nextCount++;
                                                                if (nextCount < 15) {
                                                                    return true;
                                                                } else {
                                                                    return false;
                                                                }
                                                            }
                                                            if ("getLong".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("VALUE_ID".equals(firstArgument)) {
                                                                        return 80 + nextCount;
                                                                    }
                                                                }
                                                            }
                                                            if ("getString".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("TEMPLATE_CODE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "serviceName";
                                                                            }
                                                                            case 2: {
                                                                                return "serverHost";
                                                                            }
                                                                            case 3: {
                                                                                return "serverPort";
                                                                            }
                                                                            case 4: {
                                                                                return "serverContext";
                                                                            }
                                                                            case 5: {
                                                                                return "certificateDir";
                                                                            }
                                                                            case 6: {
                                                                                return "rootCertificateFileName";
                                                                            }
                                                                            case 7: {
                                                                                return "clientCertificateFileName";
                                                                            }
                                                                            case 8: {
                                                                                return "clientKeyPathFileName";
                                                                            }
                                                                            case 9: {
                                                                                return "clientKeyPassword";
                                                                            }
                                                                            case 10: {
                                                                                return "user";
                                                                            }
                                                                            case 11: {
                                                                                return "domain";
                                                                            }
                                                                            case 12: {
                                                                                return "maxTotal";
                                                                            }
                                                                            case 13: {
                                                                                return "maxIdle";
                                                                            }
                                                                            case 14: {
                                                                                return "minIdle";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    if ("VALUE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "@QLIK_SENSE_TARGET1@";
                                                                            }
                                                                            case 2: {
                                                                                return "@QLIK_SENSE_SERVER_HOST1@";
                                                                            }
                                                                            case 3: {
                                                                                return "@QLIK_SENSE_SERVER_PORT1@";
                                                                            }
                                                                            case 4: {
                                                                                return "@QLIK_SENSE_SERVER_CONTEXT1@";
                                                                            }
                                                                            case 5: {
                                                                                return "@QLIK_SENSE_SERTIFICATE_DIR1@";
                                                                            }
                                                                            case 6: {
                                                                                return "@QLIK_SENSE_ROOT_SERTIFICATE_FILE_NAME1@";
                                                                            }
                                                                            case 7: {
                                                                                return "@QLIK_SENSE_CLIENT_SERTIFICATE_FILE_NAME1@";
                                                                            }
                                                                            case 8: {
                                                                                return "@QLIK_SENSE_CLIENT_KEY_FILE_NAME1@";
                                                                            }
                                                                            case 9: {
                                                                                return "@QLIK_SENSE_CLIENT_KEY_PASSWORD1@";
                                                                            }
                                                                            case 10: {
                                                                                return "@QLIK_SENSE_USER_NAME1@";
                                                                            }
                                                                            case 11: {
                                                                                return "@QLIK_SENSE_USER_DOMAIN1@";
                                                                            }
                                                                            case 12: {
                                                                                return "@QLIK_SENSE_POOL_MAX_TOTAL1@";
                                                                            }
                                                                            case 13: {
                                                                                return "@QLIK_SENSE_POOL_MAX_IDLE1@";
                                                                            }
                                                                            case 14: {
                                                                                return "@QLIK_SENSE_POOL_MIN_IDLE1@";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                    if ("TEMPLATE_DESC".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "1";
                                                                            }
                                                                            case 2: {
                                                                                return "2";
                                                                            }
                                                                            case 3: {
                                                                                return "3";
                                                                            }
                                                                            case 4: {
                                                                                return "4";
                                                                            }
                                                                            case 5: {
                                                                                return "5";
                                                                            }
                                                                            case 6: {
                                                                                return "6";
                                                                            }
                                                                            case 7: {
                                                                                return "7";
                                                                            }
                                                                            case 8: {
                                                                                return "8";
                                                                            }
                                                                            case 9: {
                                                                                return "9";
                                                                            }
                                                                            case 10: {
                                                                                return "10";
                                                                            }
                                                                            case 11: {
                                                                                return "11";
                                                                            }
                                                                            case 12: {
                                                                                return "12";
                                                                            }
                                                                            case 13: {
                                                                                return "13";
                                                                            }
                                                                            case 14: {
                                                                                return "14";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                            return null;
                                                        }

                                                    });
                                                    return resultSet;
                                                }
                                                if ("proxyMSSQLService1".equals(argBeanCode)) {
                                                    ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                        private int nextCount = 0;

                                                        @Override
                                                        public Object answer(InvocationOnMock invocation) throws Throwable {

                                                            Method m = ((InvocationImpl) invocation).getMethod();
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if ("next".equals(m.getName())) {
                                                                nextCount++;
                                                                if (nextCount < 9) {
                                                                    return true;
                                                                } else {
                                                                    return false;
                                                                }
                                                            }
                                                            if ("getLong".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("VALUE_ID".equals(firstArgument)) {
                                                                        return 10 + nextCount;
                                                                    }
                                                                }
                                                            }
                                                            if ("getString".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("TEMPLATE_CODE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "mssqlURL";
                                                                            }
                                                                            case 2: {
                                                                                return "mssqlUser";
                                                                            }
                                                                            case 3: {
                                                                                return "mssqlPassword";
                                                                            }
                                                                            case 4: {
                                                                                return "serviceName";
                                                                            }
                                                                            case 5: {
                                                                                return "timeout";
                                                                            }
                                                                            case 6: {
                                                                                return "forcedToTemplateUsage";
                                                                            }
                                                                            case 7: {
                                                                                return "maxIdle";
                                                                            }
                                                                            case 8: {
                                                                                return "maxActive";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    if ("VALUE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "@MSSQL_URL1@";
                                                                            }
                                                                            case 2: {
                                                                                return "@MSSQL_USER1@";
                                                                            }
                                                                            case 3: {
                                                                                return "@MSSQL_PASSWORD1@";
                                                                            }
                                                                            case 4: {
                                                                                return "@DATAPOWER_TARGET1@";
                                                                            }
                                                                            case 5: {
                                                                                return "@MSSQL_TIMEOUT@";
                                                                            }
                                                                            case 6: {
                                                                                return "@MSSQL_FORCED_TEMPLATE@";
                                                                            }
                                                                            case 7: {
                                                                                return "8";
                                                                            }
                                                                            case 8: {
                                                                                return "24";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                    if ("TEMPLATE_DESC".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "1";
                                                                            }
                                                                            case 2: {
                                                                                return "2";
                                                                            }
                                                                            case 3: {
                                                                                return "3";
                                                                            }
                                                                            case 4: {
                                                                                return "4";
                                                                            }
                                                                            case 5: {
                                                                                return "5";
                                                                            }
                                                                            case 6: {
                                                                                return "6";
                                                                            }
                                                                            case 7: {
                                                                                return "7";
                                                                            }
                                                                            case 8: {
                                                                                return "8";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                            return null;
                                                        }

                                                    });
                                                    return resultSet;

                                                }
                                                if ("proxySQLPublicService".equals(argBeanCode)) {

                                                    ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                        private int nextCount = 0;

                                                        @Override
                                                        public Object answer(InvocationOnMock invocation) throws Throwable {

                                                            Method m = ((InvocationImpl) invocation).getMethod();
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if ("next".equals(m.getName())) {
                                                                nextCount++;
                                                                if (nextCount < 2) {
                                                                    return true;
                                                                } else {
                                                                    return false;
                                                                }
                                                            }
                                                            if ("getLong".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("VALUE_ID".equals(firstArgument)) {
                                                                        return 60 + nextCount;
                                                                    }
                                                                }
                                                            }
                                                            if ("getString".equals(m.getName())) {
                                                                if (arguments.length > 0) {
                                                                    Object firstArgument = arguments[0];
                                                                    if ("TEMPLATE_CODE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "conversion";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }
                                                                    }
                                                                    if ("VALUE".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "CONVERT_TO_OLD_ALPHA";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                    if ("TEMPLATE_DESC".equals(firstArgument)) {
                                                                        switch (nextCount) {
                                                                            case 1: {
                                                                                return "1";
                                                                            }
                                                                            default: {
                                                                                break;
                                                                            }
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                            return null;
                                                        }

                                                    });
                                                    return resultSet;

                                                }
                                                if ("proxyDispatcherService".equals(argBeanCode)) {
                                                    ResultSet resultSet = Mockito.mock(ResultSet.class, new Answer() {
                                                        private int nextCount = 0;

                                                        @Override
                                                        public Object answer(InvocationOnMock invocation) throws Throwable {

                                                            Method m = ((InvocationImpl) invocation).getMethod();
                                                            Object[] arguments = ((InvocationImpl) invocation).getArguments();
                                                            if ("next".equals(m.getName())) {
                                                                nextCount++;
                                                                if (nextCount < 1) {
                                                                    return true;
                                                                } else {
                                                                    return false;
                                                                }
                                                            }
                                                            return null;
                                                        }

                                                    });
                                                    return resultSet;

                                                }

                                            }

                                            return null;
                                        }
                                    });
                                    return preparedStatement;
                                }
                            }
                        }
                    }
                    return null;
                }
            });
            return connection;
        }
        return null;
    }
}
