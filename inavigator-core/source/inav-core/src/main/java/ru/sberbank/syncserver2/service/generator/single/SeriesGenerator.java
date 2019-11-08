package ru.sberbank.syncserver2.service.generator.single;

import ru.sberbank.syncserver2.service.generator.single.data.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 27.01.12
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class SeriesGenerator {

    private static ThreadLocal<NumberFormat> decimalFormat = new ThreadLocal<NumberFormat>() {

    	@Override
        protected NumberFormat initialValue() {
			NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);
			f.setMinimumFractionDigits(0);
			f.setGroupingUsed(false);

            return f;
    	};
    };

    public RDDataSeries getSeriesFromResultSet(ResultSet rs, int seriesIndex, ETLAction action) throws SQLException {
        //1. Finding right merge columns
        String[] mergeColumns = new String[0]; //no merge by default
        if (action.getMerges() != null) {
            for (int i = 0; i < action.getMerges().size(); i++) {
                ETLActionMerge merge =  action.getMerges().get(i);
                if(merge.getSeriesIndex()==seriesIndex){
                    mergeColumns = merge.getFields().split(",");
                    break;
                }
            }
        }

        //2. Finding right change types
        List<ETLActionChangeType> seriesChangeTypes = new ArrayList<ETLActionChangeType>();
        for (int i = 0; i < action.getChangeTypes().size(); i++) {
            ETLActionChangeType t =  action.getChangeTypes().get(i);
            if(t.getSeriesIndex()==seriesIndex){
                seriesChangeTypes.add(t);
            }

        }

        //3. Generating metadata
        //System.out.println("FOUND "+changeTypes.size()+" CHANGE TYPES: "+changeTypes);
        ResultSetMetaData rsm = rs.getMetaData();
        ArrayList<RDMetaDataField> fields = new ArrayList<RDMetaDataField>();
        int columnCount = rsm.getColumnCount();
        int[] sqlTypes = new int[columnCount];
        int[] scales = new int[columnCount];
        int[] mergeColumnIndices = new int[mergeColumns.length];
        int m = 0;
        for(int i=1; i<=rsm.getColumnCount(); i++){
            String name = rsm.getColumnName(i);
            int sqlType = rsm.getColumnType(i);
            Integer scale = getNewScale(seriesChangeTypes, name);
            if (scale == null) {
                scale = rsm.getScale(i);
            }
            sqlTypes[i-1] = sqlType;
            scales[i-1] = scale;
            String javaType = getJavaTypeBySQLType(sqlType, scale);
            RDMetaDataField field = new RDMetaDataField(name,javaType);
            if(contains(mergeColumns, name)){
                mergeColumnIndices[m++] = i-1;
                if(RDMetaDataField.STRING_DATA_TYPE.equalsIgnoreCase(javaType)){
                    field.setDataType("stringArray");
                } else if (RDMetaDataField.DOUBLE_DATA_TYPE.equalsIgnoreCase(javaType)
                        || RDMetaDataField.LONG_DATA_TYPE.equalsIgnoreCase(javaType)) {
                    field.setDataType(RDMetaDataField.VALUE_ARRAY_DATA_TYPE);
//                } else if("long".equalsIgnoreCase(javaType)){
//                    field.setDataType("longArray");
//                } else if("double".equalsIgnoreCase(javaType)){
//                    field.setDataType("doubleArray");
                } else if(RDMetaDataField.DATETIME_DATA_TYPE.equalsIgnoreCase(javaType)){
                    field.setDataType(RDMetaDataField.DATE_ARRAY_DATA_TYPE);
                }
            }
            String newDataType = getNewDataType(seriesChangeTypes, name);
            //System.out.println("NEW DATA TYPE FOR "+name+" IS "+newDataType);
            if(newDataType!=null){
                field.setDataType(newDataType);
            }
            fields.add(field);
        }
        RDMetaData metaData = new RDMetaData(fields);

        //4. Generating rows and merging them if necessary
        ArrayList<RDRow> rows = new ArrayList<RDRow>();
        String[] values = new String[columnCount];
        RDRow previousRow = null;
        while(rs.next()){
            for (int i = 1; i <= columnCount; i++) {
                int sqlType = sqlTypes[i-1];
                int scale = scales[i-1];
                values[i-1] = getValue(rs, i, sqlType, scale);
            }
            RDRow row = new RDRow(values.clone());
            if(previousRow==null){
                rows.add(row);
                previousRow = row;
            } else {
                boolean merged = merge(mergeColumnIndices, previousRow, row, metaData);
                if(!merged){
                    rows.add(row);
                    previousRow = row;
                }
            }
        }

        //5. Generating series
        RDDataSeries series = new RDDataSeries();
        String name = getSeriesName(action.getNames(),seriesIndex++);
        series.setIndex(name);
        series.setMetadata(metaData);
        series.setRows(rows);
        return series;
    }

    public String getSeriesName(List<ETLSeriesName> names, int seriesIndex){
        //System.out.println("LOOKING FOR NAME FOR SERIES "+seriesIndex);
        if(names!=null){
            for (int i = 0; i < names.size(); i++) {
                ETLSeriesName name =  names.get(i);
                if(name.getSeriesIndex()==seriesIndex){
                    return name.getSeriesName();
                }
            }
        }
        return String.valueOf(seriesIndex);
    }

    private String getValue(ResultSet rs, int columnIndex, int sqlType, int scale) throws SQLException {
        switch (sqlType){
            case Types.CHAR:
            case Types.VARCHAR:
                //1. убираем лишние кавычки
                String s = rs.getString(columnIndex);
                if(s!=null){
                    s = s.replace("\"",""); //убираем лишние кавычки
                } else {
                    s = "";
                }

                //2. заменяем непечатные символы пробелами
                s = removeNotPrintableLetters(s);
                return s;
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                String stringValue;
                if(scale==0) {
                    long value = rs.getLong(columnIndex);
                    stringValue = String.valueOf(value);
                } else {
                    double value = rs.getDouble(columnIndex);
                    decimalFormat.get().setMaximumFractionDigits(scale);
                    stringValue = decimalFormat.get().format(value);
                }
                return stringValue;
            case Types.INTEGER:
            case Types.BIGINT: return rs.getString(columnIndex);
            case Types.TIMESTAMP:
            case Types.DATE:
                Date date = rs.getTimestamp(columnIndex);
                return FormatHelper.formatDate(date);
            default:
                Logger.getAnonymousLogger().severe("UNEXPECTED SQL TYPE : "+sqlType);
        }
        return "";
    }

    private String removeNotPrintableLetters(String s) {
        //1. Check for null
        if(s==null){
            return s;
        }

        //2. Replacing single not printables
        return XMLEscapeHelper.escapeCharacters(s);
    }

    private String getJavaTypeBySQLType(int sqlType, int scale) {
        switch (sqlType){
            case Types.CHAR:
            case Types.VARCHAR: return RDMetaDataField.STRING_DATA_TYPE;
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL: return scale==0 ? RDMetaDataField.LONG_DATA_TYPE : RDMetaDataField.DOUBLE_DATA_TYPE;
            case Types.INTEGER:
            case Types.BIGINT: return RDMetaDataField.LONG_DATA_TYPE;
            case Types.TIMESTAMP:
            case Types.DATE:   return RDMetaDataField.DATETIME_DATA_TYPE;
            default:
                Logger.getAnonymousLogger().severe("UNEXPECTED SQL TYPE : "+sqlType);
        }
        return "";
    }

    public boolean merge(int[] mergeColumnIndices, RDRow src1AndDest, RDRow src2, RDMetaData metaData){
        //1. Check if rows are mereable
        for(int i=0; i<src1AndDest.getFields().size(); i++){
            if(contains(mergeColumnIndices, i)){
                continue;
            }
            String value1 = src1AndDest.getFieldValue(i);
            String value2 = src2.getFieldValue(i);
            if(!equals(value1,value2)){
                return false;
            }
        }

        //2. Make merge
        for (int i = 0; i < mergeColumnIndices.length; i++) {
            int mergeColumnIndex = mergeColumnIndices[i];
            String mergedValue = src1AndDest.getFieldValue(mergeColumnIndex)+","+src2.getFieldValue(mergeColumnIndex);
            src1AndDest.setFieldValue(mergeColumnIndex, mergedValue);
        }
        return true;
    }

    private boolean contains(int[] allValues, int lookupValue){
        for (int i = 0; i < allValues.length; i++) {
            if(allValues[i]==lookupValue){
                return true;
            }
        }
        return false;
    }

    private static boolean contains(String[] allValues, String lookupValue){
        for (int i = 0; i < allValues.length; i++) {
            if(equals(allValues[i],lookupValue)){
                return true;
            }
        }
        return false;
    }

    public static String getNewDataType(List<ETLActionChangeType> changeTypes, String lookupValue){
        for (int i = 0; i < changeTypes.size(); i++) {
            ETLActionChangeType etlActionChangeType =  changeTypes.get(i);
            String[] values = etlActionChangeType.getFields().split(",");
            if(contains(values, lookupValue)){
                return etlActionChangeType.getNewDataType();
            }
        }
        return null;
    }

    private Integer getNewScale(List<ETLActionChangeType> changeTypes, String fieldName){
        for (ETLActionChangeType etlActionChangeType : changeTypes) {
            String[] values = etlActionChangeType.getFields().split(",");
            if(contains(values, fieldName)){
                return etlActionChangeType.getScale();
            }
        }
        return null;
    }

    private static boolean equals(String value1, String value2){
        if(value1==null && value2==null){
            return true;
        }
        return value1!=null && value1.equals(value2);
    }

}
