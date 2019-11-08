package ru.sberbank.syncserver2.service.core;

import org.junit.Test;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.qlikview.QlikViewRequestService;
import ru.sberbank.syncserver2.service.sql.QlikSenseService;

import java.util.ArrayList;
import java.util.List;

public class ServiceManagerHelperTest {

    @Test
    public void configureTest() throws ComponentException {
        Bean qlikServiceBean = new Bean(12L, "proxyQlikSenseService1", QlikSenseService.class.getCanonicalName(), "proxyDispatcherService",
                "subService", 12, "online.do", "Description");
        List<BeanProperty> lbp = new ArrayList<BeanProperty>();
        BeanProperty bp = new BeanProperty(1L, "serverHost", "sbt-csit-011.ca.sbrf.ru", "ddddd");
        lbp.add(bp);
        bp = new BeanProperty(1L, "serverPort", "4747", "ddddddd");
        lbp.add(bp);
        qlikServiceBean.setBeanProperties(lbp);
        QlikSenseService service = new QlikSenseService();
        ServiceManagerHelper.configure(qlikServiceBean, service);
    }

    @Test
    public void configureTest1() throws ComponentException {
        Bean qlikServiceBean = new Bean(12L, "proxyQlikViewService1", QlikViewRequestService.class.getCanonicalName(), null,
                null, 12, null, "Description");
        List<BeanProperty> lbp = new ArrayList<BeanProperty>();
        BeanProperty bp = new BeanProperty(1L, "clustered", "false", "ddddd");
        lbp.add(bp);
        bp = new BeanProperty(1L, "reentrant", "false", "ddddddd");
        lbp.add(bp);
        qlikServiceBean.setBeanProperties(lbp);
        QlikViewRequestService service = new QlikViewRequestService();
        ServiceManagerHelper.configure(qlikServiceBean, service);
    }


}
