package ru.sberbank.syncserver2.service.generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.log.DbLogService;
import ru.sberbank.syncserver2.service.log.DbLogServiceTestBase;
import ru.sberbank.syncserver2.service.log.LogMsg;
import ru.sberbank.syncserver2.util.FormatHelper;

import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/cluster-manager-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class ClusterManagerTest extends DbLogServiceTestBase {

    @Autowired
    @Qualifier("serviceManagerServiceTestBean")
    private ServiceManager serviceManager;

    @Autowired
    @Qualifier("spyClusterManagerServiceTestBean")
    private ClusterManager service;

    @Before
    @Override
    public void before() throws ComponentException, InterruptedException, SQLException {

        super.before();

        service.doInit();
    }

    @After
    public void after() {
        //
    }

    @Test
    public void clusterManagerServiceTest() {
        Assert.assertTrue(service.isActive());
    }

}
