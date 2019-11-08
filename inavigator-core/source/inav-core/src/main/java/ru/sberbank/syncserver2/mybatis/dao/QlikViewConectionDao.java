package ru.sberbank.syncserver2.mybatis.dao;

import com.thoughtworks.xstream.XStream;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import ru.sberbank.qlik.view.ObjectData;
import ru.sberbank.qlik.view.Response;
import ru.sberbank.syncserver2.mybatis.domain.QlikViewConectionProperties;
import ru.sberbank.syncserver2.mybatis.domain.QlikViewDBError;
import ru.sberbank.syncserver2.util.FormatHelper;
import ru.sberbank.syncserver2.xstream.qlikview.QlikViewDBErrorConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class QlikViewConectionDao {

//    @Autowired
//    @Qualifier("infra.bd.navigator.sqlSessionFactory")
//    private SqlSessionFactory sqlSessionFactory;

    //private XStream xs;
    //private QlikViewDBErrorConverter converter;

    public QlikViewConectionDao() {
    //    xs = new XStream();
    //    converter = new QlikViewDBErrorConverter();
    //    xs.registerConverter(converter);
    //    xs.alias("rs", QlikViewDBError.class);
    }

    public List<QlikViewConectionProperties> selectQlikViewConectionProperties(SqlSessionFactory sqlSessionFactory) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.selectList("selectQlikViewConectionProperties");
        } finally {
            session.close();
        }
    }

    public void saveQlikResponse(
            SqlSessionFactory sqlSessionFactory,
            QlikViewConectionProperties p,
            Response objectsData,
            Logger logger,
            QlikViewConectionDaoCallable<Void, String> callable) throws Exception {

        SqlSession session = sqlSessionFactory.openSession();
        try {
            if (objectsData.getData() != null) {
/*
                Map<String, String> m = new HashMap<String, String>();
                m.put("documentUri", p.getDocumentUri());
                m.put("document", objectsData.getDocument());
*/
                session.delete("deleteDocument");
                Map<String, Map<String, String>> rowMap = new HashMap<String, Map<String, String>>();
                for (ObjectData od : objectsData.getData()) {
                    if (!od.isError()) {
                        if (od.getMatrix() != null) {

                            int rowNum = 0;
                            for (List<String> rowList : od.getMatrix()) {
                                int collNum = 0;
                                for (String collumnValue : rowList) {

                                    String idKey = Integer.toString(rowNum) + "/" + od.getId();
                                    Map<String, String> row = rowMap.get(idKey);
                                    if (row == null) {
                                        row = new HashMap<String, String>();
                                        row.put("DashBoardCode", p.getDashBoardId());
                                        row.put("documentUri", p.getDocumentUri());
                                        row.put("document", objectsData.getDocument());
                                        row.put("id", od.getId());
                                        rowMap.put(idKey, row);
                                    }
                                    row.put("collumn_" + collNum, collumnValue);

                                    collNum++;
                                }
                                rowNum++;
                            }

                        } else {
                            String strMsg = FormatHelper.stringConcatenator("ObjectsData error : od.getMatrix() is null!", od);
                            logger.error(strMsg);
                            callable.onCall(strMsg);
                        }
                    } else {
                        String strMsg = FormatHelper.stringConcatenator("ObjectsData error : ", od);
                        logger.error(strMsg);
                        callable.onCall(strMsg);
                    }
                }
                for (Map<String, String> row : rowMap.values()) {
                    //logger.debug("insertDocument: " + row);
                    session.insert("insertDocument", row);
                }
                session.commit();
                Map<String, QlikViewDBError> par = new HashMap<String, QlikViewDBError>();
                par.put("pxmlOutput", null);
                session.update("qlikViewLoadComplete", par);
                session.commit();
                QlikViewDBError err = par.get("pxmlOutput");
                if (err.hasError()) {
                    String strMsg = err.getAllErrors();
                    logger.error(strMsg);
                    callable.onCall(strMsg);
                }
            } else {
                String strMsg = "objectsData.getData() == null";
                logger.error(strMsg);
                callable.onCall(strMsg);
            }

        } finally {
            session.close();
        }
    }

}
