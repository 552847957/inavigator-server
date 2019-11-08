package ru.sberbank.syncserver2.service.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;

import java.lang.reflect.Method;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/cron-cheduler-service-test.xml"})
@ActiveProfiles("unit-test")
public class CronSchedulerBackgroundServiceTest {

    @Autowired
    @Qualifier("spyCronSchedulerBackgroundServiceTestBean")
    private CronSchedulerBackgroundService service;

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void test1() throws Exception {
        Scheduler shced1 = service.getSched();
        Scheduler shced2 = service.getSched();
        Assert.assertEquals(shced1, shced2);
    }

    @Test
    public void test2() throws Exception {
        ServiceContainer sc = new ServiceContainer(Mockito.mock(ServiceManager.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                if ("getConfigLoader".equals(m.getName())) {
                    return Mockito.mock(ConfigLoader.class);
                }
                return null;
            }
        }), "qlikview", Mockito.mock(Bean.class));

        final int[] cnt = {0};
        service.setCronExpression("0/5 * * * * ?"); //every 5 seconds
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cnt[0]++;
                System.out.println(cnt[0]);
                return null;
            }
        }).when(service).doRun();
        Mockito.doReturn("test").when(service).getJobIdentity();
        Mockito.doReturn(null).when(service).getClusterManager();
        service.setReentrant(false);

        service.setServiceContainer(sc);
        sc.setService(service);
        sc.startService();

        //service.doStart();
        Thread.sleep(13000);
        //service.doStop();
        sc.stopService();

        Assert.assertTrue(cnt[0] <= 3);
    }

    @Test
    public void test3() throws Exception {
        ServiceContainer sc = new ServiceContainer(Mockito.mock(ServiceManager.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                if ("getConfigLoader".equals(m.getName())) {
                    return Mockito.mock(ConfigLoader.class);
                }
                return null;
            }
        }), "qlikview", Mockito.mock(Bean.class));

        final int[] cnt = {0};
        service.setCronExpression("0/1 * * * * ?"); //every 5 seconds
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cnt[0]++;
                System.out.println(cnt[0]);
                Thread.sleep(5000);
                return null;
            }
        }).when(service).doRun();
        Mockito.doReturn("test").when(service).getJobIdentity();
        Mockito.doReturn(null).when(service).getClusterManager();
        service.setReentrant(false);

        service.setServiceContainer(sc);
        sc.setService(service);
        sc.startService();

        //service.doStart();
        Thread.sleep(12000);
        //service.doStop();
        sc.stopService();

        Assert.assertTrue(cnt[0] <= 3);
    }

    @Test
    public void test4() throws Exception {

        ServiceContainer sc = new ServiceContainer(Mockito.mock(ServiceManager.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                if ("getConfigLoader".equals(m.getName())) {
                    return Mockito.mock(ConfigLoader.class);
                }
                return null;
            }
        }), "qlikview", Mockito.mock(Bean.class));

        final int[] cnt = {0};
        service.setCronExpression("0/1 * * * * ?"); //every 5 seconds
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                cnt[0]++;
                System.out.println(cnt[0]);
                Thread.sleep(5000);
                return null;
            }
        }).when(service).doRun();
        Mockito.doReturn("test").when(service).getJobIdentity();
        Mockito.doReturn(null).when(service).getClusterManager();
        service.setReentrant(true);

        service.setServiceContainer(sc);
        sc.setService(service);
        sc.startService();

        //service.doStart();
        Thread.sleep(12000);
        //service.doStop();
        sc.stopService();

        Assert.assertTrue(cnt[0] <= 13);
    }

}
