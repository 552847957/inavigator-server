package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 15.12.11
 * Time: 11:32
 * To change this template use File | Settings | File Templates.
 */
public class RDXMLRelationalHelper extends RDXMLColumnHelper {

    public static RDDataSeries select(RDDataSeries input, String... fieldNames){
        //1. Find correct indices for selected field names
        RDMetaData inputMetadata = input.getMetadata();
        int[] indices = new int[fieldNames.length];
        for(int i=0; i<fieldNames.length; i++){
            String fieldName = fieldNames[i];
            indices[i] = inputMetadata.getFieldIndexByName(fieldName);
            if(indices[i]==-1){
                throw new IllegalArgumentException("Invalid field name: "+fieldName);
            }
        }

        //2. Prepare output metadata
        RDMetaData outputMetadata = new RDMetaData();
        ArrayList<RDMetaDataField> outputMetadataFields = new ArrayList<RDMetaDataField>(indices.length);
        for (int i = 0; i < indices.length; i++) {
            RDMetaDataField field = inputMetadata.getFieldByIndex(indices[i]);
            outputMetadataFields.add(field);
        }
        outputMetadata.setFields(outputMetadataFields);

        //3. Prepare output rows
        ArrayList<RDRow> outputRows = null;
        if(input.getRows()!=null){
            outputRows = new ArrayList<RDRow>(input.getRows().size());
            ArrayList<RDRow> inputRows = input.getRows();
            for (int r = 0; r < inputRows.size(); r++) {
                RDRow inputRow =  inputRows.get(r);
                ArrayList<String> inputValues  = inputRow.getFields();
                ArrayList<String> outputValues = new ArrayList<String>(indices.length);
                for (int f = 0; f < indices.length; f++) {
                    String value = inputValues.get(indices[f]);
                    outputValues.add(value);
                }
                RDRow outputRow = new RDRow();
                outputRow.setFields(outputValues);
                outputRows.add(outputRow);
            }
        }

        //4. Prepare data series copy
        RDDataSeries result = new RDDataSeries();
        result.setMetadata(outputMetadata);
        result.setRows(outputRows);
        return result;
    }

    public static interface RDWhereFieldFilter {
        public boolean accept(String fieldValue);
    }

    public static RDDataSeries where(RDDataSeries input, String fieldName, RDWhereFieldFilter filter){
        //1. Clone metadata
        RDMetaData inputMetadata = input.getMetadata();
        RDMetaData outputMetadata = (RDMetaData) inputMetadata.clone();
        int fieldIndex = inputMetadata.getFieldIndexByName(fieldName);
        if(fieldIndex==-1){
            throw new IllegalArgumentException("Invalid field name: "+fieldName);
        }

        //2. Making distinct rows
        ArrayList<RDRow> inputRows = input.getRows();
        ArrayList<RDRow> outputRows = new ArrayList<RDRow>(inputRows.size());
        for (Iterator<RDRow> rowIterator = inputRows.iterator(); rowIterator.hasNext(); ) {
            RDRow row =  rowIterator.next();
            String fieldValue = row.getFields().get(fieldIndex);
            if(filter.accept(fieldValue)){
                outputRows.add(row);
            }
        }

        //4. Prepare data series copy
        RDDataSeries result = new RDDataSeries();
        result.setMetadata(outputMetadata);
        result.setRows(outputRows);
        return result;
    }

    public static RDDataSeries distinct(RDDataSeries input) {
        //1. Clone metadata
        RDMetaData inputMetadata = input.getMetadata();
        RDMetaData outputMetadata = (RDMetaData) inputMetadata.clone();

        //2. Making distinct rows
        HashSet<RDRow> outputRowSet = new HashSet<RDRow>();
        ArrayList<RDRow> inputRows = input.getRows();
        for (Iterator<RDRow> rowIterator = inputRows.iterator(); rowIterator.hasNext(); ) {
            RDRow row =  rowIterator.next();
            outputRowSet.add(row);
        }
        ArrayList<RDRow> outputRows = new ArrayList<RDRow>(outputRowSet);

        //3. Sorting
        final int fieldCount = input.getMetadata().getFields().size();
        Comparator comparator = new Comparator<RDRow>() {
            public int compare(RDRow o1, RDRow o2) {
                for(int i=0; i<fieldCount; i++){
                    String value1 = o1.getFieldValue(i);
                    String value2 = o2.getFieldValue(i);
                    if(value1==null && value2==null){
                        continue;
                    } else if(value1==null){//nulls are first
                        return 1;
                    } else if(value2==null){
                        return -1;
                    } else {
                        int cmp = value1.compareTo(value2);
                        if(cmp!=0){
                            return cmp;
                        }
                    }
                }
                return 0;
            }
        };
        Collections.sort(outputRows, comparator);

        //4. Prepare data series copy
        RDDataSeries result = new RDDataSeries();
        result.setMetadata(outputMetadata);
        result.setRows(outputRows);
        return result;
    }

    public static RDDataSeries leftJoinColumns(RDDataSeries leftTable, RDDataSeries rightTable, String[] leftColumns, String[] rightColumns, String[] columnsToCopy, String[] defaultValues){
        //1. Adding empty columns
        RDDataSeries outputSeries = (RDDataSeries) leftTable.clone();
        int[] outputIndices = new int[columnsToCopy.length];
        //System.out.println("before columns = "+outputSeries.getMetadata().getFields().size());
        for (int i = 0; i < columnsToCopy.length; i++) {
            RDMetaDataField field = rightTable.getMetadata().getFieldByName(columnsToCopy[i]);
            addEmptyColumn(outputSeries, field.getName(), field.getDataType(), field.getCaption());
            outputIndices[i] = outputSeries.getMetadata().getFields().size()-1;
        }
        //System.out.println("after columns = "+outputSeries.getMetadata().getFields().size());

        //2. Making join
        ArrayList<RDRow> outputRows = outputSeries.getRows();
        ArrayList<RDRow> rightRows  = rightTable.getRows();
        ArrayList<RDRow> dublicates = new ArrayList<RDRow>();
        int[] leftIndices = findFieldIndices(outputSeries.getMetadata(), leftColumns);
        int[] rightIndices = findFieldIndices(rightTable.getMetadata() , rightColumns);
        int[] copyInputIndices = findFieldIndices(rightTable.getMetadata() , columnsToCopy);
        for (int r = 0; r < outputRows.size(); r++) {
            RDRow outputRow =  outputRows.get(r);
            boolean found = false;
            for (int r2 = 0; r2 < rightRows.size(); r2++) {
                RDRow rightRow = rightRows.get(r2);
                if(equalsByFields(outputRow, leftIndices, rightRow, rightIndices)){
                    if(!found){
                        copyFields(rightRow, copyInputIndices, outputRow, outputIndices);
                        found = true;
                    } else {
                        RDRow dublicate = (RDRow) outputRow.clone();
                        copyFields(rightRow, copyInputIndices, outputRow, outputIndices);
                        dublicates.add(dublicate);
                    }
                }
            }
            if(!found){
                for (int i = 0; i < outputIndices.length; i++) {
                    outputRow.setFieldValue(outputIndices[i],defaultValues[i]);
                }
            }
        }

        //3. Composing final series
        return outputSeries;
    }
}
