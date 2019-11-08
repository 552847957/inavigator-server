package ru.sberbank.syncserver2.service.generator.single.data;


import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 15.12.11
 * Time: 11:37
 * To change this template use File | Settings | File Templates.
 */
public class RDXMLColumnHelper extends RDXMLBasicHelper{
    public static void addColumnCopy(RDDataSeries input, String fieldName, String newFieldName, String newFieldCaption) {
        //1. Amending metadata
        RDMetaData metaData = input.getMetadata();
        int originalFieldIndex = metaData.getFieldIndexByName(fieldName);
        RDMetaDataField reference = metaData.getFields().get(originalFieldIndex);
        RDMetaDataField copy = (RDMetaDataField) reference.clone();
        copy.setName(newFieldName);
        copy.setCaption(newFieldCaption);
        metaData.getFields().add(copy);

        //2. Amending rows
        if(input.getRows()!=null){
            ArrayList<RDRow> inputRows = input.getRows();
            for (int r = 0; r < inputRows.size(); r++) {
                RDRow inputRow =  inputRows.get(r);
                String value = inputRow.getFieldValue(originalFieldIndex);
                inputRow.getFields().add(value);
            }
        }
    }

    protected static abstract class ColumnExpression {
        private RDMetaData metadata;
        private String[]   columns;
        private int[]      indices;

        public ColumnExpression(String[] columns) {
            this.columns = columns;
        }

        protected final void setMetaData(RDMetaData metadata){
            this.metadata = metadata;
            this.indices = findFieldIndices(metadata, columns);
        }

        protected abstract String processRow(String[] values);

        public String process(RDMetaData metadata, RDRow input) {
            String[] values = findFieldValues(input, indices);
            String result = processRow(values);
            return result;
        }
    }

    public static class GreatestLong extends ColumnExpression {

        public GreatestLong(String... columns) {
            super(columns);
        }



        @Override
        protected String processRow(String[] values) {
            long greatest = Long.MIN_VALUE;
            boolean defined = false;
            for (int i = 0; i < values.length; i++) {
                String sValue = values[i];
                try {
                    long   lValue = Long.parseLong(sValue);
                    defined = true;
                    if(lValue>greatest){
                        greatest = lValue;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            return defined ? String.valueOf(greatest):"";
        }
    }

    public static class RowSumLong extends ColumnExpression {

        public RowSumLong(String... columns) {
            super(columns);
        }



        @Override
        protected String processRow(String[] values) {
            long sum = 0;
            for (int i = 0; i < values.length; i++) {
                String sValue = values[i];
                try {
                    sum += Long.parseLong(sValue);
                } catch (NumberFormatException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            return String.valueOf(sum);
        }
    }

    public static void addColumnExpression(RDDataSeries input, ColumnExpression expression, String newFieldName, String newFieldCaption) {
        //1. Prepare function for calculation
        RDMetaData metaData = input.getMetadata();
        expression.setMetaData(metaData);
        metaData.addField(newFieldName, newFieldCaption, "long");

        //2. Amending rows
        if(input.getRows()!=null){
            ArrayList<RDRow> inputRows = input.getRows();
            for (int r = 0; r < inputRows.size(); r++) {
                RDRow inputRow =  inputRows.get(r);
                String value = expression.process(metaData, inputRow);
                inputRow.getFields().add(value);
            }
        }
    }

    public static void addAutoIncrement(RDDataSeries input, String newFieldName, String newFieldCaption) {
        //1. Amending metadata
        RDMetaData metaData = input.getMetadata();
        metaData.addField(newFieldName, "int", newFieldCaption);

        //2. Amending rows
        if(input.getRows()!=null){
            ArrayList<RDRow> inputRows = input.getRows();
            for (int r = 0; r < inputRows.size(); r++) {
                RDRow inputRow =  inputRows.get(r);
                inputRow.getFields().add(String.valueOf(r + 1));
            }
        }
    }

    public static void addAutoIncrementByCombination(RDDataSeries input, String[] combination, String newFieldName, String newFieldCaption) {
        //1. Amending metadata
        RDMetaData metaData = input.getMetadata();
        metaData.addField(newFieldName, "int", newFieldCaption);
        int[] indices = findFieldIndices(metaData, combination);

        //2. Amending rows
        if(input.getRows()!=null){
            final RDXMLHelper.UniqueCombinationEnumerator enumerator = new RDXMLHelper.UniqueCombinationEnumerator();
            ArrayList<RDRow> inputRows = input.getRows();
            for (int r = 0; r < inputRows.size(); r++) {
                RDRow inputRow =  inputRows.get(r);
                String[] values = findFieldValues(inputRow, indices);
                int id = enumerator.getUniqueId(values);
                inputRow.getFields().add(String.valueOf(id));
            }
        }
    }

    public static void addEmptyColumn(RDDataSeries ref, String newFieldName, String newFieldType, String newFieldCaption) {
        //1. Amending metadata
        RDMetaData metaData = ref.getMetadata();
        metaData.addField(newFieldName, newFieldType, newFieldCaption);

        //2. Amending rows
        if(ref.getRows()!=null){
            ArrayList<RDRow> inputRows = ref.getRows();
            for (int r = 0; r < inputRows.size(); r++) {
                RDRow inputRow =  inputRows.get(r);
                inputRow.getFields().add("");
            }
        }
    }

    public static void renameColumn(RDDataSeries input, String fieldName, String newFieldName, String newFieldCaption) {
        RDMetaData metaData = input.getMetadata();
        int fieldIndex = metaData.getFieldIndexByName(fieldName);
        RDMetaDataField reference = metaData.getFields().get(fieldIndex);
        reference.setName(newFieldName);
        reference.setCaption(newFieldCaption);
    }


    public static RDDataSeries replaceColumnValues(RDDataSeries input, final String fieldName, final String[] searchValues, final String[] replaceValues){
        final int fieldIndex = input.getMetadata().getFieldIndexByName(fieldName);
        RDXMLHelper.RDRowProcessor processor = new RDXMLHelper.RDRowProcessor(){
            public RDRow process(RDMetaData metadata, RDRow input) {
                RDRow output = (RDRow) input.clone();
                String field = input.getFieldValue(fieldIndex);
                for (int i = 0; i < searchValues.length; i++) {
                    if(field.equals(searchValues[i])){
                        output.setFieldValue(fieldIndex, replaceValues[i]);
                    }
                }
                return output;
            }
        };
        return processRows(input, processor);
    }

    public static void swapColumns(RDDataSeries input, String fieldName1, String fieldName2) {
        //1. Amending metadata
        RDMetaData metaData = input.getMetadata();
        int index1 = metaData.getFieldIndexByName(fieldName1);
        int index2 = metaData.getFieldIndexByName(fieldName2);
        RDMetaDataField swap1 = metaData.getFields().get(index1);
        RDMetaDataField swap2 = metaData.getFields().get(index2);
        metaData.getFields().set(index1, swap2);
        metaData.getFields().set(index2, swap1);

        //2. Amending rows
        if(input.getRows()!=null){
            ArrayList<RDRow> inputRows = input.getRows();
            for (int r = 0; r < inputRows.size(); r++) {
                RDRow inputRow =  inputRows.get(r);
                String swapValue1 = inputRow.getFieldValue(index1);
                String swapValue2 = inputRow.getFieldValue(index2);
                inputRow.setFieldValue(index1, swapValue2);
                inputRow.setFieldValue(index2, swapValue1);
            }
        }
    }

    protected static int[] findFieldIndices(RDMetaData metaData, String[] columns){
        int[] indices = new int[columns.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = metaData.getFieldIndexByName(columns[i]);
        }
        return indices;
    }

    protected static String[] findFieldValues(RDRow row, int[] indices){
        String[] values = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            values[i] = row.getFieldValue(indices[i]);
        }
        return values;
    }

    protected static boolean equalsByFields(RDRow leftRow, int[] leftColumnIndices, RDRow rightRow, int[] rightColumnIndices) {
        if(leftColumnIndices.length!=rightColumnIndices.length){
            return false;
        }
        for (int i = 0; i < leftColumnIndices.length; i++) {
            String leftValue = leftRow.getFieldValue(leftColumnIndices[i]);
            String rightValue = rightRow.getFieldValue(rightColumnIndices[i]);
            if(!equals(leftValue,rightValue)){
                return false;
            }
        }
        return true;
    }

    protected static void copyFields(RDRow inputRow, int[] inputColumns, RDRow outputRow, int[] outputColumns) {
        if(inputColumns.length!=outputColumns.length){
            return;
        }
        for (int i = 0; i < outputColumns.length; i++) {
            String value = inputRow.getFieldValue(inputColumns[i]);
            outputRow.setFieldValue(outputColumns[i],value);
        }
    }
}
