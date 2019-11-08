package ru.sberbank.syncserver2.xstream.qlikview;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import ru.sberbank.syncserver2.mybatis.domain.QlikViewDBError;
import ru.sberbank.syncserver2.xstream.XmlAttribute;
import ru.sberbank.syncserver2.xstream.XmlConverter;

import java.util.List;

public class QlikViewDBErrorConverter extends XmlConverter implements Converter {

    private int cntThreashold = 4;

    public int getCntThreashold() {
        return cntThreashold;
    }

    public void setCntThreashold(int cntThreashold) {
        this.cntThreashold = cntThreashold;
    }

    @Override
    public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        init();
        setReader(hierarchicalStreamReader);
        QlikViewDBError returnObject = new QlikViewDBError();
        List<QlikViewDBError> esItems = returnObject.getErrorList();
        illegalXmlContent("rs");
        try {
            XmlAttribute loadId = new XmlAttribute("Load_ID", null, Long.class);
            peekNode("rs", String.class, loadId);
            returnObject.setLoadID((Long)loadId.getValue());
            int cnt = 0;
            while (hierarchicalStreamReader.hasMoreChildren()) {
                getReader().moveDown();
                try {
                    illegalXmlContent("r");
                    QlikViewDBError errItem = new QlikViewDBError();
                    try {

                        XmlAttribute errorId = new XmlAttribute("Error_ID", null, Long.class);
                        XmlAttribute kpiId = new XmlAttribute("KPI_ID", null, Long.class);
                        XmlAttribute errorNumber = new XmlAttribute("ErrorNumber", null, Long.class);
                        XmlAttribute errorMessage = new XmlAttribute("ErrorMessage", null, String.class);
                        peekNode("r", String.class,
                                errorId,
                                kpiId,
                                errorNumber,
                                errorMessage
                        );

                        errItem.setErrorID((Long) errorId.getValue());
                        errItem.setKpiID((Long) kpiId.getValue());
                        errItem.setErrorNumber((Long) errorNumber.getValue());
                        errItem.setErrorMessage((String) errorMessage.getValue());
                        esItems.add(errItem);

                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка в формате "
                                + "данных в XML.", e);
                    }
                } finally {
                    getReader().moveUp();
                }
                cnt++;
                if (cnt > cntThreashold) break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка в формате "
                    + "данных в XML.", e);
        }
        return returnObject;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return QlikViewDBError.class.equals(aClass);
    }

}
