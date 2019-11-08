package ru.sberbank.syncserver2.xstream.qlikview;

import com.thoughtworks.xstream.XStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.mybatis.domain.QlikViewDBError;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/config-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class QlikViewDBErrorConverterTest {

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void qlikViewDBErrorConverterTest1() {
        XStream xs = new XStream();
        QlikViewDBErrorConverter converter = new QlikViewDBErrorConverter();
        xs.registerConverter(converter);
        xs.alias("rs", QlikViewDBError.class);
        QlikViewDBError err = (QlikViewDBError) xs.fromXML("<rs Load_ID=\"47\">\n" +
                "<r Error_ID=\"483\" KPI_ID=\"16078\" ButchNumber=\"3\" BatchStartTime=\"2018-01-22T18:18:09.740\" ErrorNumber=\"241\" ErrorSeverity=\"16\" ErrorMessage=\"Conversion failed when converting date and/or time from character string.\" />\n" +
                "<r Error_ID=\"484\" KPI_ID=\"16079\" ButchNumber=\"2\" BatchStartTime=\"2018-01-22T18:18:09.753\" ErrorNumber=\"241\" ErrorSeverity=\"16\" ErrorMessage=\"Conversion failed when converting date and/or time from character string.\" />\n" +
                "<r Error_ID=\"485\" KPI_ID=\"16080\" ButchNumber=\"2\" BatchStartTime=\"2018-01-22T18:18:09.770\" ErrorNumber=\"241\" ErrorSeverity=\"16\" ErrorMessage=\"Conversion failed when converting date and/or time from character string.\" />\n" +
                "<r Error_ID=\"486\" KPI_ID=\"16081\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.783\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"487\" KPI_ID=\"16082\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.790\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"488\" KPI_ID=\"16083\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.803\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"489\" KPI_ID=\"16084\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.813\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"490\" KPI_ID=\"16085\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.823\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "</rs>\n");
        System.out.println(err);
        Assert.assertEquals(err.getErrorList().size(), 5);
        if (err.hasError()) {
            System.out.println(err.getAllErrors());
        } else {
            Assert.assertTrue("It must be errors!", false);
        }
    }

    @Test
    public void qlikViewDBErrorConverterTest2() {
        XStream xs = new XStream();
        QlikViewDBErrorConverter converter = new QlikViewDBErrorConverter();
        xs.registerConverter(converter);
        xs.alias("rs", QlikViewDBError.class);
        QlikViewDBError err = (QlikViewDBError) xs.fromXML("<rs Load_ID=\"47\">\n" +
                "<r Error_ID=\"483\" KPI_ID=\"16078\" ButchNumber=\"3\" BatchStartTime=\"2018-01-22T18:18:09.740\" ErrorNumber=\"241\" ErrorSeverity=\"16\" ErrorMessage=\"Conversion failed when converting date and/or time from character string.\">eeeeee</r>\n" +
                "<r Error_ID=\"484\" KPI_ID=\"16079\" ButchNumber=\"2\" BatchStartTime=\"2018-01-22T18:18:09.753\" ErrorNumber=\"241\" ErrorSeverity=\"16\" ErrorMessage=\"Conversion failed when converting date and/or time from character string.\" />\n" +
                "<r Error_ID=\"485\" KPI_ID=\"16080\" ButchNumber=\"2\" BatchStartTime=\"2018-01-22T18:18:09.770\" ErrorNumber=\"241\" ErrorSeverity=\"16\" ErrorMessage=\"Conversion failed when converting date and/or time from character string.\" />\n" +
                "<r Error_ID=\"486\" KPI_ID=\"16081\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.783\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"487\" KPI_ID=\"16082\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.790\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"488\" KPI_ID=\"16083\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.803\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"489\" KPI_ID=\"16084\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.813\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "<r Error_ID=\"490\" KPI_ID=\"16085\" ButchNumber=\"1\" BatchStartTime=\"2018-01-22T18:18:09.823\" ErrorNumber=\"2812\" ErrorSeverity=\"16\" ErrorMessage=\"Could not find stored procedure 'qwe'.\" />\n" +
                "</rs>\n");
        System.out.println(err);
        Assert.assertEquals(err.getErrorList().size(), 5);
    }

    @Test
    public void qlikViewDBErrorConverterTest3() {
        XStream xs = new XStream();
        QlikViewDBErrorConverter converter = new QlikViewDBErrorConverter();
        xs.registerConverter(converter);
        xs.alias("rs", QlikViewDBError.class);
        QlikViewDBError err = (QlikViewDBError) xs.fromXML("<rs Load_ID=\"47\">\n" +
                "</rs>\n");
        System.out.println(err);
        Assert.assertEquals(err.getErrorList().size(), 0);
    }

}
