package ru.sberbank.syncserver2.service.generator.single.data;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 15.12.11
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class RDXMLAggregateHelper extends RDXMLRelationalHelper {
    public static abstract class AggregateFunction{
        private Map<CombinedKey,Number> results = new HashMap<CombinedKey, Number>();
        private String columnName;
        private int columnIndex;
        private Class type;

        protected AggregateFunction(Class type, String columnName) {
            this.type = type;
            this.columnName = columnName;
        }

        public void reset(){
            results.clear();
        }

        public String getColumnName() {
            return columnName;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        public Number parse(String value){
            if(type==Long.class){
                return Long.parseLong(value);
            } else if(type==Double.class){
                return Double.parseDouble(value);
            } else {
                return null;
            }
        }

        public final void process(CombinedKey key, String rowValue){
            Number numberValue = parse(rowValue);
            Number overallValue = results.get(key);
            Number newOverallValue = process(key, overallValue,numberValue);
            results.put(key, newOverallValue);
        }

        public Map<CombinedKey, Number> getResults() {
            return results;
        }

        public abstract Number process(CombinedKey key, Number overallValue, Number rowValue);
    }

    public static class CountLong extends AggregateFunction{

        public CountLong() {
            super(Long.class,null);
        }

        @Override
        public Number process(CombinedKey key, Number overallValue, Number rowValue) {
            long newOverallValue = (overallValue==null ? 0:overallValue.longValue()) + 1;
            return new Long(newOverallValue);
        }
    }

    public static class CountDouble extends AggregateFunction{

        public CountDouble() {
            super(Double.class,null);
        }

        @Override
        public Number process(CombinedKey key, Number overallValue, Number rowValue) {
            double newOverallValue = (overallValue==null ? 0:overallValue.doubleValue()) + 1;
            return new Double(newOverallValue);
        }
    }

    public static class SumLong extends AggregateFunction{

        public SumLong(String columnName) {
            super(Long.class, columnName);
        }

        @Override
        public Number process(CombinedKey key, Number overallValue, Number rowValue) {
            long newOverallValue = (overallValue==null ? 0:overallValue.longValue()) + rowValue.longValue();
            return new Long(newOverallValue);
        }
    }

    public static class SumDouble extends AggregateFunction{

        public SumDouble(String columnName) {
            super(Double.class, columnName);
        }

        @Override
        public Number process(CombinedKey key, Number overallValue, Number rowValue) {
            double newOverallValue = (overallValue==null ? 0:overallValue.doubleValue()) + rowValue.doubleValue();
            return new Double(newOverallValue);
        }
    }

    public static class PartitionSumLong extends AggregateFunction{
        private int rowIndex = 0;

        @Override
        public void reset() {
            super.reset();    //To change body of overridden methods use File | Settings | File Templates.
            rowIndex = 0;
        }

        public PartitionSumLong(String columnName) {
            super(Long.class, columnName);
        }

        @Override
        public Number process(CombinedKey key, Number overallValue, Number rowValue) {
            rowIndex++;
            if(rowIndex>1){
                long newOverallValue = (overallValue==null ? 0:overallValue.longValue()) + rowValue.longValue();
                return new Long(newOverallValue);
            } else {
                return overallValue;
            }
        }
    }


    public static class MaxLong extends AggregateFunction{

        public MaxLong(String columnName) {
            super(Long.class, columnName);
        }

        @Override
        public Number process(CombinedKey key, Number overallValue, Number rowValue) {
            if(overallValue==null){
                return rowValue;
            } else if(rowValue==null){
                return overallValue;
            } else if(overallValue.doubleValue()<rowValue.doubleValue()){
                return rowValue;
            } else {
                return overallValue;
            }
        }
    }

    public static class MinLong extends AggregateFunction{

        public MinLong(String columnName) {
            super(Long.class, columnName);
        }

        @Override
        public Number process(CombinedKey key, Number overallValue, Number rowValue) {
            if(overallValue==null){
                return rowValue;
            } else if(rowValue==null){
                return overallValue;
            } else if(overallValue.doubleValue()>rowValue.doubleValue()){
                return rowValue;
            } else {
                return overallValue;
            }
        }
    }

    public static RDDataSeries calcCounts(RDDataSeries input, String... groupColumns) {
        //1. Finding column indices
        int[] indices = new int[groupColumns.length];
        RDMetaData inputMetadata = input.getMetadata();
        for (int i = 0; i < indices.length; i++) {
            indices[i] = inputMetadata.getFieldIndexByName(groupColumns[i]);
        }

        //2. Calculating counts
        ArrayList<RDRow> inputRows = input.getRows();
        Map<CombinedKey,Integer> counts = new HashMap<CombinedKey,Integer>();
        for (int i = 0; i < inputRows.size(); i++) {
            RDRow rdRow =  inputRows.get(i);
            Object[] data = new Object[groupColumns.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = rdRow.getFieldValue(indices[j]);
            }
            CombinedKey key = new CombinedKey(data);
            Integer count = (Integer) counts.get(key);
            if(count!=null){
                counts.put(key, new Integer(count.intValue()+1));
            } else {
                counts.put(key, new Integer(1));
            }
        }

        //3. Translating result map to series
        //3.1. Preparing metadata
        RDMetaData outputMetadata = new RDMetaData();
        for (int i = 0; i < indices.length; i++) {
            RDMetaDataField inputField = inputMetadata.getFieldByIndex(indices[i]);
            RDMetaDataField outputField = (RDMetaDataField) inputField.clone();
            outputMetadata.addField(inputField.getName(), inputField.getDataType(), inputField.getCaption());
        }
        outputMetadata.addField("count","int","Количество");

        //3.2. Preparing rows
        ArrayList<RDRow> outputRows = new ArrayList<RDRow>(counts.size());
        for (Iterator iterator = counts.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<CombinedKey,Integer> entry = (Map.Entry<CombinedKey,Integer>) iterator.next();
            RDRow rdRow = new RDRow();
            CombinedKey key = entry.getKey();
            ArrayList<String> fields = new ArrayList<String>();
            for(int i=0; i<indices.length; i++){
                fields.add(String.valueOf(key.data[i]));
            }
            fields.add(String.valueOf(entry.getValue()));
            rdRow.setFields(fields);
            outputRows.add(rdRow);
        }

        //4. Preparing result
        RDDataSeries outputSeries = new RDDataSeries();
        outputSeries.setIndex("0");
        outputSeries.setMetadata(outputMetadata);
        outputSeries.setRows(outputRows);
        return outputSeries;
    }

    public static void addPartitionAggregate(RDDataSeries input, String[] groupColunmns, AggregateFunction function, String newName, String newCaption) {
        //1. Finding field index
        RDMetaData metaData = input.getMetadata();
        int functionFieldIndex = metaData.getFieldIndexByName(function.getColumnName());

        //2. Calculating and adding
        CombinedKey prevKey   = null;
        Number      prevTotal = null;
        ArrayList<RDRow> rows = input.getRows();
        int[] indices = findFieldIndices(input.getMetadata(), groupColunmns);
        for (int i = 0; i < rows.size(); i++) {
            //2.1. Creating group key
            RDRow row =  rows.get(i);
            CombinedKey currKey = new CombinedKey(row, indices);
            if(!currKey.equals(prevKey)){
                function.reset();
                prevTotal = null;
            }

            //2.2. Calculating
            String sValue = row.getFieldValue(functionFieldIndex);
            Number nValue = function.parse(sValue);
            Number currTotal = function.process(currKey, prevTotal, nValue);
            row.getFields().add(prevTotal==null ? "0":String.valueOf(prevTotal));

            //2.3. Go to the next
            prevKey = currKey;
            prevTotal = currTotal;
        }

        //3. Modifying metadata
        metaData.addField(newName, newCaption, "long");
    }

    public static RDDataSeries calcOverallSummaries(RDDataSeries input, String[]groupColumns, AggregateFunction[] functions) {
        //1. Check if we have anything to calculate
        if(functions.length==0){
            return null;
        }

        //2. For every group column we fill -1
        RDMetaData inputMetadata = input.getMetadata();
        int[] keyIndexInMetaData = new int[groupColumns.length];
        int[] metaDataIndexInKey = new int[inputMetadata.getFields().size()];
        for (int i = 0; i < inputMetadata.getFields().size(); i++) {
            metaDataIndexInKey[i] = -1;
        }
        for (int i = 0; i < keyIndexInMetaData.length; i++) {
            int metaDataIndex = inputMetadata.getFieldIndexByName(groupColumns[i]);
            keyIndexInMetaData[i] = metaDataIndex;
            metaDataIndexInKey[metaDataIndex] = i;
        }

        //2. Resetting functions
        for (int i = 0; i < functions.length; i++) {
            functions[i].reset();
            String columnName = functions[i].getColumnName();
            int index = inputMetadata.getFieldIndexByName(columnName);
            functions[i].setColumnIndex(index);
        }

        //3. Calculating summaries
        ArrayList<RDRow> inputRows = input.getRows();
        for (int i = 0; i < inputRows.size(); i++) {
            RDRow rdRow =  inputRows.get(i);
            Object[] data = new Object[groupColumns.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = rdRow.getFieldValue(keyIndexInMetaData[j]);
            }

            CombinedKey key = new CombinedKey(data);
            for(int f=0; f<functions.length; f++){
                int columnIndex = functions[f].getColumnIndex();
                String columnValue = rdRow.getFieldValue(columnIndex);
                functions[f].process(key, columnValue);
            }
        }

        //3. Translating result map to series
        RDMetaData outputMetadata = (RDMetaData) inputMetadata.clone();
        Map<CombinedKey,Number> results = functions[0].getResults();
        ArrayList<RDRow> outputRows = new ArrayList<RDRow>(results.size());
        for (Iterator iterator = results.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<CombinedKey,Number> entry = (Map.Entry<CombinedKey,Number>) iterator.next();
            RDRow rdRow = new RDRow();
            CombinedKey key = entry.getKey();
            ArrayList<String> fields = new ArrayList<String>(metaDataIndexInKey.length);
            for(int i=0; i<metaDataIndexInKey.length; i++){
                if(metaDataIndexInKey[i]!=-1){
                    fields.add(String.valueOf(key.data[ metaDataIndexInKey[i] ]));
                } else {
                    fields.add("");
                }
            }
            for(int i=0; i<functions.length; i++){
                Number value = i==0 ? entry.getValue() : functions[i].results.get(key);
                int fieldIndex = functions[i].getColumnIndex();
                fields.set(fieldIndex, String.valueOf(value));
            }
            rdRow.setFields(fields);
            outputRows.add(rdRow);
        }

        //4. Preparing result
        RDDataSeries outputSeries = new RDDataSeries();
        outputSeries.setIndex("0");
        outputSeries.setMetadata(outputMetadata);
        outputSeries.setRows(outputRows);
        return outputSeries;
    }

    public static void addMissingSummaries(RDDataSeries base, RDDataSeries summaries, String[] groupColumns) {
        //1. Finding indices
        RDMetaData inputMetadata = base.getMetadata();
        int[] keyIndexInMetaData = new int[groupColumns.length];
        int[] metaDataIndexInKey = new int[inputMetadata.getFields().size()];
        for (int i = 0; i < inputMetadata.getFields().size(); i++) {
            metaDataIndexInKey[i] = -1;
        }
        for (int i = 0; i < keyIndexInMetaData.length; i++) {
            int metaDataIndex = inputMetadata.getFieldIndexByName(groupColumns[i]);
            keyIndexInMetaData[i] = metaDataIndex;
            metaDataIndexInKey[metaDataIndex] = i;
        }

        //2. Finding all existing
        Set<CombinedKey> existing = new HashSet<CombinedKey>();
        ArrayList<RDRow> inputRows = base.getRows();
        for (int i = 0; i < inputRows.size(); i++) {
            RDRow rdRow =  inputRows.get(i);
            Object[] data = new Object[groupColumns.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = rdRow.getFieldValue(keyIndexInMetaData[j]);
            }

            CombinedKey key = new CombinedKey(data);
            existing.add(key);
        }

        //2. Adding all not existing
        ArrayList<RDRow> summaryRows = summaries.getRows();
        for (int i = 0; i < summaryRows.size(); i++) {
            RDRow rdRow =  summaryRows.get(i);
            Object[] data = new Object[groupColumns.length];
            for (int j = 0; j < data.length; j++) {
                data[j] = rdRow.getFieldValue(keyIndexInMetaData[j]);
            }

            CombinedKey key = new CombinedKey(data);
            if(!existing.contains(key)){
                inputRows.add(rdRow);
            }
        }
    }


    private static class CombinedKey {
        private Object[] data;

        private CombinedKey(Object[] data) {
            this.data = data;
        }

        private CombinedKey(RDRow row, int[] indices) {
            this.data = new Object[indices.length];
            for (int i = 0; i < indices.length; i++) {
                data[i] = row.getFields().get(indices[i]);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CombinedKey that = (CombinedKey) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(data, that.data)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return data != null ? Arrays.hashCode(data) : 0;
        }

        static int[] findIndices(RDMetaData metaData, String[] fields){
            int[] indices = new int[fields.length];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = metaData.getFieldIndexByName(fields[i]);
            }
            return indices;
        }
    }

    public static class UniqueCombinationEnumerator {
        private HashMap<CombinedKey, Integer> combinations = new HashMap<CombinedKey, Integer>();

        public int getUniqueId(Object... elements){
            CombinedKey key = new CombinedKey(elements);
            Integer id = combinations.get(key);
            if(id==null){
                id = combinations.size()+1;
                combinations.put(key, id);
            }
            return id.intValue();
        }
    }
}
