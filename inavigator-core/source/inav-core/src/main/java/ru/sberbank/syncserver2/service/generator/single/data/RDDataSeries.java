package ru.sberbank.syncserver2.service.generator.single.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Created by IntelliJ IDEA.
 * User: Leonid
 * Date: 17.11.2011
 * Time: 9:29:29
 * To change this template use File | Settings | File Templates.
 */
@XmlType(name = "series")
public class RDDataSeries implements Cloneable{
    private String index;
    private RDMetaData metadata;
    private ArrayList<RDRow> rows = new ArrayList<RDRow>();

    @XmlAttribute(name = "index")
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public String toString(){
        return "  <series index=\""+index+"\">\n    "+metadata+"\n"+ RDXMLHelper.toString(rows)+"\n  </series>";
    }

    @XmlElement(name = "metadata")
    public RDMetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(RDMetaData metadata) {
        this.metadata = metadata;
    }

    @XmlElement(name = "row")
    public ArrayList<RDRow> getRows() {
        return rows;
    }

    public void setRows(ArrayList<RDRow> rows) {
        this.rows = rows;
    }

    @Override
    public Object clone() {
        RDDataSeries result = null;
        try {
            result = (RDDataSeries) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
        if(this.metadata!=null){
            result.metadata = (RDMetaData) this.metadata.clone();
        }
        if(this.rows!=null){
            result.rows = new ArrayList<RDRow>(this.rows.size());
            for (int i = 0; i < rows.size(); i++) {
                RDRow rdRow = (RDRow) rows.get(i).clone();
            }
        }
        result.rows = this.rows==null ? null : (ArrayList<RDRow>) this.rows.clone();
        return result;    //To change body of overridden methods use File | Settings | File Templates.
    }
}
