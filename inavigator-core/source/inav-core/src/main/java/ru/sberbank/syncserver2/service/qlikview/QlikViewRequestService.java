package ru.sberbank.syncserver2.service.qlikview;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import ru.sberbank.qlik.view.QlikViewClient;
import ru.sberbank.qlik.view.QlikViewClientRequest;
import ru.sberbank.qlik.view.Response;
import ru.sberbank.syncserver2.mybatis.dao.QlikViewConectionDao;
import ru.sberbank.syncserver2.mybatis.dao.QlikViewConectionDaoCallable;
import ru.sberbank.syncserver2.mybatis.domain.QlikViewConectionProperties;
import ru.sberbank.syncserver2.service.core.CronSchedulerBackgroundService;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.FormatHelper;

import java.util.List;
import java.util.concurrent.Callable;

public class QlikViewRequestService extends CronSchedulerBackgroundService {

    private Logger logger = Logger.getLogger(QlikViewRequestService.class);
    public static final String CLASSPATH_RU_SBERBANK_SYNCSERVER2_MYBATIS_SQL_MAPER_CONFIG_XML = "ru/sberbank/syncserver2/mybatis/sql_maper_config.xml";

    @Autowired
    @Qualifier("qlikViewConectionDao")
    private QlikViewConectionDao qlikViewConectionDao;

    private String qlikviewDocumentUser;
    private String qlikviewDocumentPassword;

    private String qlikviewDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String qlikviewUrl;
    private String qlikviewUser;
    private String qlikviewPassword;
    private SqlSessionFactory qlikviewSqlSessionFactory;

    private String stageDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String stageUrl;
    private String stageUser;
    private String stagePassword;
    private SqlSessionFactory stageSqlSessionFactory;


    public QlikViewRequestService() {
        super();
    }

    public String getQlikviewUrl() {
        return qlikviewUrl;
    }

    public void setQlikviewUrl(String qlikviewUrl) {
        this.qlikviewUrl = qlikviewUrl;
    }

    public String getQlikviewUser() {
        return qlikviewUser;
    }

    public void setQlikviewUser(String qlikviewUser) {
        this.qlikviewUser = qlikviewUser;
    }

    public String getQlikviewPassword() {
        return qlikviewPassword;
    }

    public void setQlikviewPassword(String qlikviewPassword) {
        this.qlikviewPassword = qlikviewPassword;
    }

    public String getStageUrl() {
        return stageUrl;
    }

    public void setStageUrl(String stageUrl) {
        this.stageUrl = stageUrl;
    }

    public String getStageUser() {
        return stageUser;
    }

    public void setStageUser(String stageUser) {
        this.stageUser = stageUser;
    }

    public String getStagePassword() {
        return stagePassword;
    }

    public void setStagePassword(String stagePassword) {
        this.stagePassword = stagePassword;
    }

    public String getQlikviewDocumentUser() {
        return qlikviewDocumentUser;
    }

    public void setQlikviewDocumentUser(String qlikviewDocumentUser) {
        this.qlikviewDocumentUser = qlikviewDocumentUser;
    }

    public String getQlikviewDocumentPassword() {
        return qlikviewDocumentPassword;
    }

    public void setQlikviewDocumentPassword(String qlikviewDocumentPassword) {
        this.qlikviewDocumentPassword = qlikviewDocumentPassword;
    }

    public String getQlikviewDriver() {
        return qlikviewDriver;
    }

    public void setQlikviewDriver(String qlikviewDriver) {
        this.qlikviewDriver = qlikviewDriver;
    }

    public String getStageDriver() {
        return stageDriver;
    }

    public void setStageDriver(String stageDriver) {
        this.stageDriver = stageDriver;
    }

    public QlikViewConectionDao getQlikViewConectionDao() {
        return qlikViewConectionDao;
    }

    public void setQlikViewConectionDao(QlikViewConectionDao qlikViewConectionDao) {
        this.qlikViewConectionDao = qlikViewConectionDao;
    }

    public SqlSessionFactory getQlikviewSqlSessionFactory() {
        return qlikviewSqlSessionFactory;
    }

    public void setQlikviewSqlSessionFactory(SqlSessionFactory qlikviewSqlSessionFactory) {
        this.qlikviewSqlSessionFactory = qlikviewSqlSessionFactory;
    }

    public SqlSessionFactory getStageSqlSessionFactory() {
        return stageSqlSessionFactory;
    }

    public void setStageSqlSessionFactory(SqlSessionFactory stageSqlSessionFactory) {
        this.stageSqlSessionFactory = stageSqlSessionFactory;
    }

    @Override
    protected void doStart() {
        log(LogEventType.SERV_START, "starting service");
        try {

            SqlSessionFactoryBean qlikviewFactory = new SqlSessionFactoryBean();
            qlikviewFactory.setDataSource(new UnpooledDataSource(qlikviewDriver, qlikviewUrl, qlikviewUser, qlikviewPassword));
            qlikviewFactory.setConfigLocation(new ClassPathResource(CLASSPATH_RU_SBERBANK_SYNCSERVER2_MYBATIS_SQL_MAPER_CONFIG_XML));
            qlikviewSqlSessionFactory = qlikviewFactory.getObject();

            SqlSessionFactoryBean stageFactory = new SqlSessionFactoryBean();
            stageFactory.setDataSource(new UnpooledDataSource(stageDriver, stageUrl, stageUser, stagePassword));
            stageFactory.setConfigLocation(new ClassPathResource(CLASSPATH_RU_SBERBANK_SYNCSERVER2_MYBATIS_SQL_MAPER_CONFIG_XML));
            stageSqlSessionFactory = stageFactory.getObject();

            super.doStart();

            log(LogEventType.SERV_START, "started service");
        } catch (Exception e) {
            log(LogEventType.ERROR, "Error starting service");
            logger.error(FormatHelper.stringConcatenator("QlikViewRequestService: Error start service '", e.getMessage(), "'", e));
        }

    }

    @Override
    public void doRun() throws Exception {
        //notifyCsps("Test");
        List<QlikViewConectionProperties> ls = qlikViewConectionDao.selectQlikViewConectionProperties(qlikviewSqlSessionFactory);
        if (ls.size() > 0) {
            logger.debug(FormatHelper.stringConcatenator("qlikViewConectionDao.selectQlikViewConectionProperties() return :", ls));
            for (QlikViewConectionProperties qlikViewConectionProperties : ls) {
                qlikViewConectionProperties.setUser(qlikviewDocumentUser);
                qlikViewConectionProperties.setPassword(qlikviewDocumentPassword);
                Response objectsData = getObjectsData(qlikViewConectionProperties);
                //todo: temporary
                logger.info(objectsData);
                //
                if (!objectsData.isError()) {
                    saveQlikResponse(qlikViewConectionProperties, objectsData);
                } else {
                    String strMsg = FormatHelper.stringConcatenator("Error QlikView request: '", objectsData.getErrorMessage(), "'", "\r\n", objectsData);
                    log(LogEventType.ERROR, strMsg);
                    logger.error(strMsg);
                    notifyCsps(strMsg);
                }
            }
        } else {
            log(LogEventType.DEBUG, "qlikViewConectionDao.selectQlikViewConectionProperties() return nothing!");
            logger.debug("qlikViewConectionDao.selectQlikViewConectionProperties() return nothing!");
        }
    }

    @Override
    public void doOnThrowAnyException(Throwable exception) {
        String strMsg = FormatHelper.stringConcatenator("Error QlikViewRequestService.doRun : '", exception.getMessage(), "'");
        logErrorH(LogEventType.ERROR, strMsg, exception);
        logger.error(strMsg, exception);
    }

    @Override
    public void doBeforeRun() {

    }

    @Override
    public void doAfterRun() {

    }

    @Override
    protected ClusterManager getClusterManager() {
        return super.getClusterManager();
    }

    public void saveQlikResponse(QlikViewConectionProperties p, Response objectsData) throws Exception {
        log(LogEventType.DEBUG, FormatHelper.stringConcatenator("Save request data from qlikView for ", p));
        qlikViewConectionDao.saveQlikResponse(stageSqlSessionFactory, p, objectsData, logger, new QlikViewConectionDaoCallable<Void, String>() {
            @Override
            public Void onCall(String strMsg) throws Exception {
                notifyCsps(strMsg);
                return null;
            }
        });
        log(LogEventType.DEBUG, FormatHelper.stringConcatenator("Success save request data from qlikView for ", p));
    }

    public Response getObjectsData(QlikViewConectionProperties p) {
        log(LogEventType.DEBUG, FormatHelper.stringConcatenator("Request data from qlikView for ", p));
        QlikViewClientRequest clientRequest = new QlikViewClientRequest();
        clientRequest.setDocumentUri(p.getDocumentUri());
        clientRequest.setUser(p.getUser());
        clientRequest.setPassword(p.getPassword());
        clientRequest.setObjectIds(p.getOnlyObjectIds());
        clientRequest.setDeleteCvs(true);
        clientRequest.setQuit(true);
        Response result = QlikViewClient.getObjectsData(clientRequest);
        //Response result = obtainResponseObject();

        log(LogEventType.DEBUG, FormatHelper.stringConcatenator("Success request data from qlikView for ", p));
        return result;
    }

/*
    private Response obtainResponseObject() {
        Response rspns = new Response();
        rspns.setDocument("qvp://nlb-user-bi-psi/retail_HQ/АРМ Р CORE BANKING/QVDevt/AppsTier4/ARM R CORE BANKING.qvw");
        List<ObjectData> lod = new ArrayList<ObjectData>();

        ObjectData od = new ObjectData();
        od.setId("CH1039");

        List<String> lstCollumn = new ArrayList<String>();
        lstCollumn.add("?KPI Период");  //columns=[?KPI Период,  , % вып.]
        lstCollumn.add(null);
        lstCollumn.add("% вып.");
        od.setColumns(lstCollumn);

        // [[1кв.17, 128,0, 99%], [2кв.17, 132,1, 101%], [3кв.17, 134,2, 104%], [окт.17 - ноя.17, 86,2, ]]
        List<List<String>> lls = new ArrayList<List<String>>();
        List<String> ls = new ArrayList<String>();
        ls.add("1кв.17");
        ls.add("128,0");
        ls.add("99%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("2кв.17");
        ls.add("132,1");
        ls.add("101%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("3кв.17");
        ls.add("134,2");
        ls.add("104%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("окт.17 - ноя.17");
        ls.add("86,2");
        ls.add(null);
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("");
        lod.add(od);

        // ObjectData{id='CH1040', columns=[?, ], matrix=[[          ФАКТ, 86,2], [    ПЛАН, 43,6], [   % вып., 198%]], error=false, errorMessage='null', title=''}
        od = new ObjectData();
        od.setId("CH1040");

        lstCollumn = new ArrayList<String>();
        lstCollumn.add("?");
        lstCollumn.add(null);
        od.setColumns(lstCollumn);

        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("          ФАКТ");
        ls.add("86,2");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("    ПЛАН");
        ls.add("43,6");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("   % вып.");
        ls.add(" 198%");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("");
        lod.add(od);

        //ObjectData{id='CH1052', columns=[?KPI Период, факт, % выполнения], matrix=[[1кв.17, 11560684731,7, 102,1%], [2кв.17, 11719536464,4, 102,3%], [3кв.17, 11368557426,4, 100,9%],
        //[окт.17 - ноя.17, 11293654166,5, 100,8%]], error=false, errorMessage='null', title='СДО квартальный (по плановому курсу), (трлн.руб.)'}
        od = new ObjectData();
        od.setId("CH1052");

        lstCollumn = new ArrayList<String>();
        lstCollumn.add("?KPI Период, факт");
        lstCollumn.add("факт");
        lstCollumn.add("% выполнения");
        od.setColumns(lstCollumn);

        // [[1кв.17, 11560684731,7, 102,1%], [2кв.17, 11719536464,4, 102,3%], [3кв.17, 11368557426,4, 100,9%], [окт.17 - ноя.17, 11293654166,5, 100,8%]]
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("1кв.17");
        ls.add("11560684731,7");
        ls.add("102,1%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("2кв.17");
        ls.add("11719536464,4");
        ls.add("102,3%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("3кв.17");
        ls.add("11368557426,4");
        ls.add("100,9%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("окт.17 - ноя.17");
        ls.add("11293654166,5");
        ls.add("100,8%");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("СДО квартальный (по плановому курсу), (трлн.руб.)");
        lod.add(od);


        // ObjectData{id='CH1054', columns=[?, ], matrix=[[          ФАКТ, 11,3], [    ПЛАН, 22,4], [   % вып., 101%]], error=false, errorMessage='null', title=''}
        od = new ObjectData();
        od.setId("CH1054");

        lstCollumn = new ArrayList<String>();
        lstCollumn.add("?");
        lstCollumn.add(null);
        od.setColumns(lstCollumn);

        //
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("          ФАКТ");
        ls.add("11,3");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("    ПЛАН");
        ls.add(" 22,4");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("   % вып.");
        ls.add(" 101%");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("");
        lod.add(od);

        //ObjectData{id='CH1184', columns=[?=Date(НачалоКвартала,'DD.MM.YY'),  , % вып.], matrix=[[01.01.17, 35,0%, ], [01.04.17, 34,7%, 99,2%], [01.07.17, 34,5%, 100,1%], [01.10.17, 36,4%, 103,6%]], error=false, errorMessage='null', title=''}
        od = new ObjectData();
        od.setId("CH1184");

        lstCollumn = new ArrayList<String>();
        lstCollumn.add("?=Date(НачалоКвартала,'DD.MM.YY')");
        lstCollumn.add(null);
        lstCollumn.add(" % вып.");
        od.setColumns(lstCollumn);

        //[[01.01.17, 35,0%, ], [01.04.17, 34,7%, 99,2%], [01.07.17, 34,5%, 100,1%], [01.10.17, 36,4%, 103,6%]]
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("01.01.17");
        ls.add(" 35,0%");
        ls.add(null);
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("01.04.17");
        ls.add(" 34,7%");
        ls.add(" 99,2%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("01.07.17");
        ls.add(" 34,5%");
        ls.add(" 100,1%]");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("01.10.17");
        ls.add(" 36,4%");
        ls.add(" 103,6%");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("");
        lod.add(od);

        //ObjectData{id='CH674', columns=[?, ], matrix=[[                ФАКТ, 36,4%], [                ПЛАН, 35,1%], [% вып., 103,6%]], error=false, errorMessage='null', title=''}
        od = new ObjectData();
        od.setId("CH674");

        lstCollumn = new ArrayList<String>();
        lstCollumn.add("?");
        lstCollumn.add(null);
        od.setColumns(lstCollumn);

        //[[                ФАКТ, 36,4%], [                ПЛАН, 35,1%], [% вып., 103,6%]]
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("                ФАКТ");
        ls.add(" 36,4%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("                ПЛАН");
        ls.add(" 35,1%");
        lls.add(ls);
        ls = new ArrayList<String>();
        ls.add("% вып.");
        ls.add(" 103,6%");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("");
        lod.add(od);

        //ObjectData{id='TX3826', columns=[], matrix=[[На дату: 01.12.2017]], error=false, errorMessage='null', title='Доходы по продуктам Трайба,\r\nмлрд.руб.'}
        od = new ObjectData();
        od.setId("TX3826");
        od.setColumns(null);

        //[[На дату: 01.12.2017]]
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("На дату: 01.12.2017");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("Доходы по продуктам Трайба,\r\nмлрд.руб.");
        lod.add(od);

        //ObjectData{id='TX3828', columns=[], matrix=[[На дату: 01.12.2017]], error=false, errorMessage='null', title='CДО привлечения ФЛ,
        //трлн.руб.'}
        od = new ObjectData();
        od.setId("TX3828");
        od.setColumns(null);

        //[[На дату: 01.12.2017]]
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("На дату: 01.12.2017");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("CДО привлечения ФЛ,\r\nтрлн.руб.");
        lod.add(od);

        //ObjectData{id='TX3829', columns=[], matrix=[[На дату: 01.12.2017]], error=false, errorMessage='null', title='Доля рынка кредитования
        //ПК и КК, %'}], error=false, errorMessage='null'}
        od = new ObjectData();
        od.setId("TX3829");
        od.setColumns(null);

        //ObjectData{id='TX3829', columns=[], matrix=[[На дату: 01.12.2017]], error=false, errorMessage='null', title='Доля рынка кредитования
        //ПК и КК, %'}], error=false, errorMessage='null'}
        lls = new ArrayList<List<String>>();
        ls = new ArrayList<String>();
        ls.add("На дату: 01.12.2017");
        lls.add(ls);
        od.setMatrix(lls);
        od.setError(false);
        od.setErrorMessage("null");
        od.setTitle("Доля рынка кредитования\r\nПК и КК, %");
        lod.add(od);

        rspns.setData(lod);
        return rspns;
    }
*/

}
