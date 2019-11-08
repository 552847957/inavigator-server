package ru.sberbank.syncserver2.service.sql.query;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.util.XMLEscapeHelper;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Sergey Erin
 */
public class ResultSetExtractorImpl implements ResultSetExtractor<DataResponse> {

    private final Map<String, FieldType> expectedTypes;
    private final DataResponse response;
    private Map<Integer, FieldType> fieldTypeBySqlType;

    public DataResponse getResponse() {
        return response;
    }

    /**
     * @param expectedTypes
     * @param response
     * @param fieldTypeBySqlType
     */
    ResultSetExtractorImpl(Map<String, FieldType> expectedTypes,
                           DataResponse response,
                           Map<Integer, FieldType> fieldTypeBySqlType) {

        this.expectedTypes = expectedTypes;
        this.response = response;
        this.fieldTypeBySqlType = fieldTypeBySqlType;
    }

    @Override
    public DataResponse extractData(ResultSet resultSet) {
        DatasetMetaData datasetMetaData = new DatasetMetaData();
        //SwitchingDataSourceDAO.log.debug("Extracting metadata");
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try {
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            for (int columnIndex = 1; columnIndex <= rsMetaData.getColumnCount(); columnIndex++) {
                String name = rsMetaData.getColumnName(columnIndex);
                FieldType type = findFieldType(expectedTypes, rsMetaData, columnIndex);

                datasetMetaData.addField(new DatasetFieldMetaData(name, type));
            }

            Dataset dataset = new Dataset();
            //SwitchingDataSourceDAO.log.debug("Extracting data");
            while (resultSet.next()) {
                DatasetRow row = new DatasetRow();
                int i = 1;
                for (DatasetFieldMetaData meta : datasetMetaData.getFields()) {
                    if (FieldType.BLOB == meta.getType()) {
//                            SwitchingDataSourceDAO.log.debug("Reading BLOB field '" + rsMetaData.getColumnName(i) + "'");
                        byte[] bytes = resultSet.getBytes(i);
//                            SwitchingDataSourceDAO.log.debug("Encode '" + rsMetaData.getColumnName(i) + "' using base64");
                        String encoded = bytes != null ? encodeBase64(bytes) : "";
                        row.addValue(encoded);

//                            Blob blob = resultSet.getBlob(i);
//                            if(blob!=null){
//                                InputStream is = blob.getBinaryStream();
//                                copyToOutputStream(is, result);
//                                byte[] bytes = result.toByteArray();
//                                log.debug("Encode '" + rsMetaData.getColumnName(i) + "' using base64");
//                                String encoded = bytes != null? Base64.encodeBase64String(bytes) : "";
//                                log.debug("Encoded BLOB = " + encoded);
//                                row.addValue(encoded);
//                            } else {
//                                row.addValue("");
//                            }
                    } else {
                        Object object = resultSet.getObject(i);
                        String encoded = object != null ? XMLEscapeHelper.escapeCharacters(object + "") : "";
//                            SwitchingDataSourceDAO.log.debug("Encoded non-BLOB = " + encoded);
                        row.addValue(encoded);
                    }
                    i++;
                }
                dataset.addRow(row);
            }
            response.setDataset(dataset);
            datasetMetaData.setRowCount(dataset.getRows() != null ? dataset.getRows().size() : 0);
            response.setResult(Result.OK);
        } catch (SQLException e) {
            SwitchingDataSourceDAO.log.error("Error processing online request", e);
            response.setResult(Result.FAIL_DB);
            response.setError("The request could be proceed: " + e.getMessage());
        }

        response.setMetadata(datasetMetaData);

        return response;
    }

    private FieldType findFieldType(Map<String, FieldType> expectedTypes, ResultSetMetaData meta, int columnIndex) throws SQLException {
        String columnName = meta.getColumnName(columnIndex);
        FieldType type = expectedTypes.get(columnName);
        String dbInfo = "";
        if (type == null) {
            int rsColumnType = meta.getColumnType(columnIndex);
            type = fieldTypeBySqlType.get(rsColumnType);
            if (type == null)
                type = FieldType.NUMBER;

            dbInfo = " (" + rsColumnType + ")";
        }

        //SwitchingDataSourceDAO.log.debug("Column type for " + columnName + ": " + type + dbInfo);

        return type;
    }

    /**
     * Workaround для обхода проблемы генерации Base64 строки с помощтю вызова метода
     * org.apache.commons.codec.binary.Base64.encodeBase64String
     * В версии 1.4 apache codecs он возвращал многотросчный base64 результат. Что не позволяет использовать его в приложении
     * В версии 1.5 apache codecs все работает корректно. Но для совместимостти со старыми версиями либ,
     * необходимо использовать данный метод перевода массима байт в строку.
     *
     * @param source
     * @return
     */
    private static String encodeBase64(byte[] source) {
        byte[] result = Base64.encodeBase64(source);
        try {
            return new String(result, CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}