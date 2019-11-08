package ru.sberbank.syncserver2.gui.data;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: kozhleo
 * Date: Oct 20, 2009
 * Time: 11:04:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomChildItem implements Comparable{
    private String id;
    private String caption;
    private String tag;

    public CustomChildItem() {
        this("","");
    }

    public CustomChildItem(int id, int caption) {
        this.id = String.valueOf(id);
        this.caption = String.valueOf(caption);
    }

    public CustomChildItem(int id, String caption) {
        this.id = String.valueOf(id);
        this.caption = caption;
    }

    public CustomChildItem(String id) {
        this.id = id;
        this.caption = id;
    }

    public CustomChildItem(String id, String caption) {
        this.id = id;
        this.caption = caption;
    }

    public CustomChildItem(ResultSet rs) throws SQLException {
        this.id = rs.getString(1);
        this.caption = rs.getString(2);

        int columnCount = rs.getMetaData().getColumnCount();
        if (columnCount > 2) {
            tag = rs.getString(3);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomChildItem item = (CustomChildItem) o;

        if (caption != null ? !caption.equals(item.caption) : item.caption != null) return false;
        if (id != null ? !id.equals(item.id) : item.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (caption != null ? caption.hashCode() : 0);
        return result;
    }



    public int compareTo(Object o) {
        //1. Casting/processing null
        CustomChildItem another = (CustomChildItem) o;
        if(another==null){
            return -1;
        }

        //2. Order by key
        if(id==null && another.id!=null){
            return -1;
        } else if(id!=null && another.id==null){
            return 1;
        } else if(id!=null && another.id!=null && !id.equals(another.id )){
            return id.compareTo(another.id);
        }

        //3. Order by caption
        int compareValue = caption!=null ? caption.compareTo(another.caption)
                                         : (another.caption!=null ? - another.caption.compareTo(caption) : 0);
        return compareValue;
    }

    @Override
    public String toString() {
        return "CustomChildItem [id=" + id + ", caption=" + caption + "]";
    }



    public static SQLDescriptor descriptor = new SQLDescriptor(){

        public String composeSQL(Object o, int queryType) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String composePrepareSQL(int queryType) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setParameters(Object o, PreparedStatement st, int queryType) throws SQLException {
//To change body of implemented methods use File | Settings | File Templates.
        }

        public Object newInstance(java.sql.ResultSet rs) throws SQLException {
            return new CustomChildItem(rs);
        }
    };
}
