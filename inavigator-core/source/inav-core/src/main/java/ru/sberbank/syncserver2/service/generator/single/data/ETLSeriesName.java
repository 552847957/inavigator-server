package ru.sberbank.syncserver2.service.generator.single.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 10.02.2012
 * Time: 15:17:57
 * To change this template use File | Settings | File Templates.
 */
public class ETLSeriesName {
    private int seriesIndex;
    private String seriesName;
    private String customSQLInTarget;

    public ETLSeriesName() {
    }

    public ETLSeriesName(int seriesIndex, String seriesName) {
        this.seriesIndex = seriesIndex;
        this.seriesName = seriesName;
    }

    @XmlAttribute
    public int getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(int seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    @XmlAttribute
    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    @XmlAttribute
    public String getCustomSQLInTarget() {
        return customSQLInTarget;
    }

    public void setCustomSQLInTarget(String customSQLInTarget) {
        this.customSQLInTarget = customSQLInTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ETLSeriesName)) return false;

        ETLSeriesName that = (ETLSeriesName) o;

        if (seriesIndex != that.seriesIndex) return false;
        if (seriesName != null ? !seriesName.equals(that.seriesName) : that.seriesName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = seriesIndex;
        result = 31 * result + (seriesName != null ? seriesName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ETLSeriesName{" +
                "seriesIndex=" + seriesIndex +
                ", seriesName='" + seriesName + '\'' +
                '}';
    }
}
