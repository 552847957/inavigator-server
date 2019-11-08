package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 24.03.2012
 * Time: 11:14:52
 * To change this template use File | Settings | File Templates.
 */
public class ETLActionParamComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        ETLActionParam p1= (ETLActionParam) o1;
        ETLActionParam p2= (ETLActionParam) o2;
        int index1 = p1.getIndex();
        int index2 = p2.getIndex();
        return (index1<index2 ? -1 : (index1==index2 ? 0 : 1));
    }
}
