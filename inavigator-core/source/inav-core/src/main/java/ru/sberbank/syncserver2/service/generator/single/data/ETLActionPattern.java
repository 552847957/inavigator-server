package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;


/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 27.01.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class ETLActionPattern {
    private String jndi;
    private String database;
    private String application;
    private String patternName;
    private List<String> queries = new ArrayList<String>();
    private ArrayList<ETLActionMerge> merges = new ArrayList<ETLActionMerge>();
    private ArrayList<ETLSeriesName>  names = new ArrayList<ETLSeriesName>();
    private ArrayList<ETLActionChangeType> changeTypes = new ArrayList<ETLActionChangeType>();
    private ArrayList<ETLCheck> checks = new ArrayList<ETLCheck>();
    private boolean formattedOutput;

    public ETLActionPattern() {
        this.formattedOutput = true;
    }

    @XmlAttribute(name="jndi")
    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    @XmlAttribute(name="database")
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @XmlAttribute(name="application")
    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    @XmlAttribute(name="patternName")
    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }


    @XmlElement(name = "query")
    public List<String> getQueries() {
        return queries;
    }

    public void setQueries(List<String> queries) {
        this.queries = queries;
    }

    public ArrayList<String> getQueriesWithParamValues(ArrayList<ETLActionParam> params){
        ArrayList<String> result = new ArrayList<String>(queries.size());
        for (int i = 0; i < queries.size(); i++) {
            String original =  queries.get(i);
            if(params!=null && original!=null){
                for (int j = 0; j < params.size(); j++) {
                    ETLActionParam param =  params.get(j);
                    if(param!=null && param.getValue()!=null){
                        original = original.replaceAll("@"+param.getName()+"@", param.getValue());
                    }
                }
            }
            result.add(original);
        }
        return result;
    }

    @XmlElement(name = "merge")
    public ArrayList<ETLActionMerge> getMerges() {
        return merges;
    }

    public void setMerges(ArrayList<ETLActionMerge> merges) {
        this.merges = merges;
    }

    @XmlElement(name = "change-type")
    public ArrayList<ETLActionChangeType> getChangeTypes() {
        return changeTypes;
    }

    public void setChangeTypes(ArrayList<ETLActionChangeType> changeTypes) {
        this.changeTypes = changeTypes;
    }

    @XmlElement(name = "name")
    public ArrayList<ETLSeriesName> getNames() {
        return names;
    }

    public void setNames(ArrayList<ETLSeriesName> names) {
        this.names = names;
    }

    @XmlAttribute(name="formattedOutput")
    public boolean isFormattedOutput() {
        return formattedOutput;
    }

    public void setFormattedOutput(boolean formattedOutput) {
        this.formattedOutput = formattedOutput;
    }

    @XmlElement(name="check")
    public ArrayList<ETLCheck> getChecks() {
        return checks;
    }

    public void setChecks(ArrayList<ETLCheck> checks) {
        this.checks = checks;
    }

    @Override
    public String toString() {
        return "ETLActionPattern{" +
                "database='" + database + '\'' +
                ", queries=" + queries +
                ", merges=" + merges +
                ", names=" + names +
                ", changeTypes=" + changeTypes +
                ", formattedOutput=" + formattedOutput +
                '}';
    }
}
