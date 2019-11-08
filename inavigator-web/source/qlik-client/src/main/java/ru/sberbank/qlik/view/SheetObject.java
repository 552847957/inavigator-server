package ru.sberbank.qlik.view;

import au.com.bytecode.opencsv.CSVReader;
import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static ru.sberbank.qlik.view.QlikViewComUtils.evaluates;
import static ru.sberbank.qlik.view.QlikViewComUtils.getStringPropertyValue;

public class SheetObject {
    public static final Logger log = LogManager.getLogger(SheetObject.class);
    private final Dispatch objectDispatch;

    public SheetObject(Dispatch objectDispatch) {
        this.objectDispatch = objectDispatch;
    }

    Dispatch getObjectDispatch() {
        return objectDispatch;
    }

    public String getObjectId() {
        String getObjectId = Dispatch.call(objectDispatch, "GetObjectId").getString();
        return getObjectId;
    }

    public ObjectData getObjectData(boolean deleteCvs) {
        String getObjectId = getObjectId();

        ObjectData objectData = new ObjectData();

        String objectName = getObjectId.replaceAll("\\\\", "_");
        QVObject.Type getObjectType = getObjectType();
        System.out.println(getObjectId + ":" + getObjectType);

        IFrame iFrame = getFrameDef();

        objectData.setTitle(iFrame.getName());

        try {
            switch (getObjectType) {
                case Text: {
                    TextObject textObject = new TextObject(this);
                    textObject.fillObjectData(objectData);
                    break;
                }

                case Straight:
                case Table:
                case Bar:
                case Pivot:
                case Combo: {
                    boolean DbIsTable = Dispatch.call(objectDispatch, "DbIsTable").getBoolean();
                    if(DbIsTable) {
                        //String tableAsText = getTableAsText(true);
                        List<Column> columns = dbGetTableInfo();
                        //dbGetTableData(objectDispatch, new int[]{0,1});
                        columns.size();
                    }

                    Dispatch getProperties = Dispatch.call(objectDispatch, "GetProperties").getDispatch();
                    Dispatch layout = Dispatch.call(getProperties, "GraphLayout").getDispatch();

                    //printDimensions(getProperties);

                    String windowTitle1 = "WindowTitle";
                    String v = getStringPropertyValue(layout, windowTitle1);
                    objectData.setTitle(v);
                    File file = new File(new File(".").getAbsoluteFile(), objectName + ".csv");
                    String absolutePath = file.getAbsolutePath();
                    exportToCsv(absolutePath);


                    Reader fr = null;
                    if (file.exists()) {
                        try {
                            fr = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
                            try {
                                CSVReader csvReader = new CSVReader(fr, ';');
                                try {
                                    objectData.setMatrix(new ArrayList<List<String>>());
                                    String[] strings = null;
                                    int index = 0;
                                    while ((strings = csvReader.readNext()) != null) {
                                        List<String> result = evaluates(objectDispatch, strings);
                                        if (index == 0) {
                                            objectData.setColumns(result);
                                        } else {
                                            objectData.getMatrix().add(result);
                                        }
                                        index++;
                                    }
                                } finally {
                                    csvReader.close();
                                }
                            } finally {
                                fr.close();
                            }
                        } finally {
                            if (fr != null) {
                                try {
                                    fr.close();
                                } catch (Exception e) {
                                    log.error(e);
                                }
                            }
                            try {
                                if (deleteCvs) {
                                    boolean delete = file.delete();
                                    if (delete) {
                                        log.debug("File " + file.getAbsolutePath() + " was deleted.");
                                    }
                                }
                            } catch (Exception e) {
                                log.error("Cant delete file: " + file.getAbsolutePath());
                            }
                        }
                    } else {
                        log.error("File " + file.getAbsolutePath() + " isn`t exist!");
                    }
                    break;
                }
                default:
                    throw new Exception("Unsupported object type");
            }
        } catch (Exception e) {
            log.error(e);
            objectData.setError(true);
            objectData.setErrorMessage(e.getMessage());
        }
        return objectData;
    }

    private void exportToCsv(String absolutePath) {
        log.debug("Export to:" + absolutePath);
        Dispatch.call(objectDispatch, "Export", absolutePath, ";", 65001, false);
        //Dispatch.call(objectDispatch, "ExportEx", absolutePath, 1, false, ";", 65001); // Просто больше настроек
        log.debug("Export to:" + absolutePath + " success!");
        if(!new File(absolutePath).exists()){
            log.error("But file " + absolutePath + "wan't created :(");
        }
    }

    private void printDimensions(Dispatch getProperties) {
        Dispatch dimensions = Dispatch.call(getProperties, "Dimensions").getDispatch();
        int count = Dispatch.get(dimensions, "Count").getInt();
        for (int d = 0; d < count; d++) {
            Dispatch dimension = Dispatch.call(dimensions, "Item", d).getDispatch();
            Dispatch title = Dispatch.call(dimension, "Title").getDispatch();
            String dimensionTitle = Dispatch.get(title, "v").getString();
            log.debug("Dimension " + d + ": " + dimensionTitle);
        }
    }

    private IFrame getFrameDef() {
        Dispatch frameDef = Dispatch.call(objectDispatch, "GetFrameDef").getDispatch();
        IFrame iFrame = new IFrame();
        iFrame.setName(getStringPropertyValue(frameDef, "Name"));
        return iFrame;
    }

    private String getTableAsText(boolean includeLabels) {
        return Dispatch.call(objectDispatch, "GetTableAsText", includeLabels).getString();
    }

    // TODO Данные из таблицы получить не получилось
    public void dbGetTableData(Dispatch dispatch, int[] columns) {
        boolean[] booleans = new boolean[0];
//        for (int i = 0; i < columns.length; i++) {
//            booleans[i] = false;
//        }
        //Dispatch dbGetTableData = Dispatch.call(dispatch, "DbGetTableData", columns, null, 0, 1000).getDispatch();
        Dispatch dbGetTableData = Dispatch.call(dispatch, "DbGetTableData", columns, booleans, 0, 2).getDispatch();
        SafeArray data = Dispatch.get(dbGetTableData, "Data").toSafeArray();
        int lBound = data.getLBound();
        int uBound = data.getUBound();
        for (int i = lBound; i <= uBound; i++) {
            String string = data.getString(i);
        }
    }

    public QVObject.Type getObjectType() {
        return QVObject.Type.resolveById(Dispatch.call(objectDispatch, "GetObjectType").getInt());
    }

    /**
     * Получение информации о таблице
     * @return
     */
    private List<Column> dbGetTableInfo() {
        Dispatch dbGetTableInfo = Dispatch.call(objectDispatch, "DbGetTableInfo").getDispatch();
        Dispatch ColumnAttrs = Dispatch.get(dbGetTableInfo, "ColumnAttrs").toDispatch();
        int count = Dispatch.get(ColumnAttrs, "Count").getInt();
        List<Column> columns = new ArrayList<Column>();
        for (int c = 0; c < count; c++) {
            Column column = new Column();
            columns.add(column);
            Dispatch item = Dispatch.call(ColumnAttrs, "Item", c).getDispatch();
            Column.Type type = Column.Type.resolve(Dispatch.get(item, "Type").getInt());
            column.type = type;
        }
        SafeArray ColumnNames = Dispatch.get(dbGetTableInfo, "ColumnNames").toSafeArray();
        int lBound = ColumnNames.getLBound();
        int uBound = ColumnNames.getUBound();
        int elemSize = ColumnNames.getElemSize();
        String[] names = new String[elemSize];
        for (int i = lBound; i <= uBound; i++) {
            columns.get(i).name = ColumnNames.getString(i);
        }
        return columns;
    }
}
