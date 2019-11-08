package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 27.01.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ETLAction {
    public static final String REPORT_KEY= "REPORT_KEY";
    public static final String REPORT_STATUS= "REPORT_STATUS";

    @XmlAttribute(name = "dataFile")
    private String dataFileName;

    @XmlAttribute(name = "patternName")
    private String patternName;

    @XmlAttribute(name = "frequencySeconds")
    private int frequencySeconds;

    @XmlElement(name = "param")
    private ArrayList<ETLActionParam> params = new ArrayList<ETLActionParam>();

    @XmlTransient
    private ETLActionPattern patternObject;

    @XmlAttribute(name = "autoRun")
    private boolean autoRun=true;

    @XmlAttribute(name = "manualRun")
    private boolean manualRun;

    @XmlAttribute(name = "saveReportHist")
    private boolean saveReportHist=true;

    @XmlAttribute(name = "caption")
    private String caption;

    @XmlAttribute(name = "reportKey")
    private String reportKey;

    @XmlAttribute(name = "reportStatus")
    private String reportStatus;

    @XmlAttribute
    private GenerationMode generationMode = GenerationMode.ON_CONDITION;

    private String configFileName;
    
    @XmlTransient
    private boolean forcePublish;

    public boolean isForcePublish() {
		return forcePublish;
	}

	public void setForcePublish(boolean forcePublish) {
		this.forcePublish = forcePublish;
	}

	public ETLAction() {
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public ETLActionPattern getPatternObject() {
        return patternObject;
    }

    public void setPatternObject(ETLActionPattern patternObject) {
        this.patternObject = patternObject;
    }

    public int getFrequencySeconds() {
        return frequencySeconds;
    }

    public void setFrequencySeconds(int frequencySeconds) {
        this.frequencySeconds = frequencySeconds;
    }

    public ArrayList<String> getQueriesWithParamValues(){
        ArrayList<String> result = new ArrayList<String>(patternObject.getQueries().size());
        for (int i = 0; i < patternObject.getQueries().size(); i++) {
            String original =  patternObject.getQueries().get(i);
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

    public ArrayList<ETLActionParam> getParams() {
        return params;
    }

    public void setParams(ArrayList<ETLActionParam> params) {
        this.params = params;
    }

    public String getDatabase(){
        return patternObject.getDatabase();
    }

    public String getJndi(){
        return patternObject.getJndi();
    }

    public ArrayList<ETLActionMerge> getMerges(){
        return patternObject.getMerges();
    }

    public ArrayList<ETLSeriesName> getNames(){
        return patternObject.getNames();
    }

    public ArrayList<ETLActionChangeType> getChangeTypes(){
        return patternObject.getChangeTypes();
    }

    public boolean isFormattedOutput(){
        return patternObject.isFormattedOutput();
    }


    private static transient ETLActionParamComparator comparator = new ETLActionParamComparator();

    public void sortParams(){
        Collections.sort(params, comparator);
    }

    public String getParamQueryWithPrevParamValues(int paramIndex) {
        ETLActionParam param = params.get(paramIndex);
        String query = param.getQuery();
        for(int i=0; i<paramIndex;i++){
            ETLActionParam previousParam =  params.get(i);
            if(previousParam!=null && previousParam.getValue()!=null){
                query = query.replaceAll("@"+previousParam.getName()+"@", previousParam.getValue());
            }
        }
        return query;
    }


    public boolean isManualRun() {
        return manualRun;
    }

    public void setManualRun(boolean manualRun) {
        this.manualRun = manualRun;
    }

    public boolean isAutoRun() {
        return autoRun;
    }

    public void setAutoRun(boolean autoRun) {
        this.autoRun = autoRun;
    }
    public String getReportKey(){
        if (reportKey==null)
          synchronized(this){
            for(ETLActionParam param: params){
                if (REPORT_KEY.equalsIgnoreCase(param.getName())){
                    reportKey = param.getConstValue()!=null?param.getConstValue():param.getValue();
                }
            }
        }
        return reportKey;
    }

    public void setReportKey(String reportKey) {
        this.reportKey = reportKey;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public boolean isSaveReportHist() {
        return saveReportHist;
    }

    public void setSaveReportHist(boolean saveReportHist) {
        this.saveReportHist = saveReportHist;
    }

    public GenerationMode getGenerationMode() {
        return generationMode;

    }

    public void setGenerationMode(GenerationMode generationMode) {
        this.generationMode = generationMode;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    @Override
    public String toString() {
        return ru.sberbank.syncserver2.util.FormatHelper.stringConcatenator("ETLAction [dataFileName=" , dataFileName , ", patternName=" , patternName , ", frequencySeconds="
                , frequencySeconds , ", params=" , params , ", patternObject=" , patternObject , ", autoRun=" , autoRun
                , ", manualRun=" , manualRun , ", saveReportHist=" , saveReportHist , ", caption=" , caption
                , ", reportKey=" , reportKey , ", reportStatus=" , reportStatus , ", generationMode=" , generationMode);
    }

    /**
     * Получить название действия applicationCode_fileName
     * @return
     */
    public String getFullName() {
    	return 
    			(getPatternObject()!=null && getPatternObject().getApplication() != null?
    					getPatternObject().getApplication().toUpperCase():"") 
    			+ "_" 
    			+ (getDataFileName()!=null?getDataFileName().toUpperCase():"");		
    }
}
