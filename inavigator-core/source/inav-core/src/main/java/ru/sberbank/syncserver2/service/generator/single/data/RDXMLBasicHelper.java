package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: SBT-Kozhinskiy-LB
 * Date: 15.12.11
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */
public class RDXMLBasicHelper {
    public static interface RDRowProcessor {
        public RDRow process(RDMetaData metadata, RDRow input);
    }

    public static RDDataSeries processRows(RDDataSeries input, RDRowProcessor processor){
        //1. Clone metadata
        RDMetaData inputMetadata = input.getMetadata();
        RDMetaData outputMetadata = (RDMetaData) inputMetadata.clone();

        //2. Making distinct rows
        ArrayList<RDRow> inputRows = input.getRows();
        ArrayList<RDRow> outputRows = new ArrayList<RDRow>(inputRows.size());
        for (Iterator<RDRow> rowIterator = inputRows.iterator(); rowIterator.hasNext(); ) {
            RDRow inputRow =  rowIterator.next();
            RDRow outputRow = processor.process(outputMetadata, inputRow);
            if(outputRow!=null){
                outputRows.add(outputRow);
            }
        }

        //4. Prepare data series copy
        RDDataSeries result = new RDDataSeries();
        result.setMetadata(outputMetadata);
        result.setRows(outputRows);
        return result;
    }


    protected static boolean equals(String v1, String v2){
        if(v1==null && v2==null){
            return true;
        } else if(v1!=null){
            return v1.equals(v2);
        } else {
            return false;
        }
    }

    public static String escapeXML(String s){
        StringBuffer result = new StringBuffer();
        for(int i=0; i<s.length(); i++){
            char c = s.charAt(i);
            switch(c){
                case '<': result.append("&lt;"); break;
                case '>': result.append("&gt;"); break;
                case '&': result.append("&amp;"); break;
                default : result.append(c);
            }
        }
        return  result.toString();
    }


    public static void escapeXML(RDRow r){
        ArrayList<String> fields = r.getFields();
        for (int i = 0; i < fields.size(); i++) {
            String s =  fields.get(i);
            s = escapeXML(s);
            fields.set(i,s);
        }
    }

    public static RDDataSeries escapeXML(RDDataSeries input){
        RDDataSeries output = (RDDataSeries) input.clone();
        ArrayList<RDRow> rows = output.getRows();
        for (int i = 0; i < rows.size(); i++) {
            RDRow rdRow =  rows.get(i);
            escapeXML(rdRow);
        }
        return output;
    }

    public static final String toString(List values){
        if(values==null || values.size()==0){
            return "";
        }

        StringBuilder sb = new StringBuilder("\n");
        for (int i = 0; i < values.size(); i++) {
            Object o =  values.get(i);
            sb.append(String.valueOf(o));
            sb.append("\n");
        }

        return sb.toString();
    }

}
