package ru.sberbank.syncserver2.service.generator.single.data;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 31.07.14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ETLCheck {
    
    @XmlAttribute(name = "name")
	private String name;

    @XmlAttribute(name = "target")
    private String target;

    @XmlAttribute(name = "type")
    private String type;

    @XmlValue
    private String query;

    @XmlAttribute(name = "operator")
    private String operator;

    @XmlAttribute(name = "rightValue")
    private String rightValue;

    @XmlAttribute(name = "minRightValue")
    private String minRightValue;

    @XmlAttribute(name = "maxRightValue")
    private String maxRightValue;

    public static interface TARGETS {
        public static final String MSSQL       = "MSSQL";
        public static final String SQLITE      = "SQLITE";
    }

    public static interface OPERATORS {
        public static final String BETWEEN     = "between";
        public static final String LIST        = "list";
        public static final String EQUALS      = "equals";
    }

    public static interface CHECK_ERROR_TYPES {
        public static final int CONDITION  	   = 1;
        public static final int OPERATOR	   = 2;
        public static final int NO_DATA        = 3;
        public static final int EXCEPTION      = 4;
        public static final int TARGET	       = 5;
    }

    /**
     * Считаем ли ошибкой генерации скрипта - начличие ошибок в самом определении проверок
     */
    public static final boolean IS_FAIL_ON_CHECK_DEFINITION_ERROR = true;
    
    /**
     *  Максимальное количество сообщений об ошибках, которые сохраняются в рамках одной проверки типа list 
     *  Если задать значение 0 - то unlimited
     */
    public static final int MAX_RESULT_ERRORS_IN_LIST_CHECK = 10;
    
    public ETLCheck() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRightValue() {
        return rightValue;
    }

    public void setRightValue(String rightValue) {
        this.rightValue = rightValue;
    }

    public String getMinRightValue() {
        return minRightValue;
    }

    public void setMinRightValue(String minRightValue) {
        this.minRightValue = minRightValue;
    }

    public String getMaxRightValue() {
        return maxRightValue;
    }

    public void setMaxRightValue(String maxRightValue) {
        this.maxRightValue = maxRightValue;
    }

    public List<ETLCheckError> check(ResultSet rs, String logObjectName, AbstractService service){
        if(OPERATORS.BETWEEN.equalsIgnoreCase(operator) || OPERATORS.EQUALS.equalsIgnoreCase(operator)){
            return checkScalar(rs, logObjectName,service);
        } else if(OPERATORS.LIST.equalsIgnoreCase(operator)){
            return checkList(rs, logObjectName,service);
        } else {
        	if (IS_FAIL_ON_CHECK_DEFINITION_ERROR) {
        		ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.OPERATOR,"Failed to pass check "+name+" for "+logObjectName+" because operator " + operator + " is invalid.");
        		return Collections.singletonList(error);
        	} else
        		return Collections.EMPTY_LIST;
        }
    }

    private List<ETLCheckError> checkList(ResultSet rs, String logObjectName, AbstractService service) {
        ArrayList<ETLCheckError> results = new ArrayList<ETLCheckError>();
        try {
        	int i = 1;
            while(rs.next() && ((i<=MAX_RESULT_ERRORS_IN_LIST_CHECK) || (MAX_RESULT_ERRORS_IN_LIST_CHECK == 0))) {
                int errorCode = rs.getInt(1);
                String errorDescription = rs.getString(2);
                ETLCheckError error = new ETLCheckError(errorCode,errorDescription);
                results.add(error);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private List<ETLCheckError> checkScalar(ResultSet rs, String logObjectName, AbstractService service) {
        try {
            if(rs.next()){
                int value = rs.getInt(1);
                if(OPERATORS.EQUALS.equalsIgnoreCase(operator)){
                    if(value==Integer.parseInt(rightValue)){
                        return Collections.EMPTY_LIST;
                    } else {
                        ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.CONDITION,"Failed to pass check "+name+" for "+logObjectName+" . The result is equal "+value);
                        return Collections.singletonList(error);
                    }
                } else if(OPERATORS.BETWEEN.equalsIgnoreCase(operator)){
                    if(Integer.parseInt(minRightValue)<=value && value<=Integer.parseInt(maxRightValue)){
                        return Collections.EMPTY_LIST;
                    } else {
                        ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.CONDITION,"Failed to pass check "+name+" for "+logObjectName+" . The result is equal "+value);
                        return Collections.singletonList(error);
                    }
                } else {
                	if (IS_FAIL_ON_CHECK_DEFINITION_ERROR) {
                		ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.OPERATOR,"Failed to pass check "+name+" for "+logObjectName+" because operator "+operator+" is invalid. The result is equal "+ value);
                		return Collections.singletonList(error);
                	} else
                		return Collections.EMPTY_LIST;
                }
            } else {
            	if (IS_FAIL_ON_CHECK_DEFINITION_ERROR) {
            		ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.NO_DATA,"Failed to pass check "+name+" for "+logObjectName+" because NO DATA returned for query {"+query+"}");
            		return Collections.singletonList(error);
            	} else
            		return Collections.EMPTY_LIST;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        	if (IS_FAIL_ON_CHECK_DEFINITION_ERROR) {
        		ETLCheckError error = new ETLCheckError(CHECK_ERROR_TYPES.EXCEPTION,"Failed to pass check "+name+" for "+logObjectName+" due to exception: "+e.getMessage());
        		return Collections.singletonList(error);
        	} else
        		return Collections.EMPTY_LIST;
        }
    }

    public static void main(String[] args) {
        ETLConfig config = new ETLConfig();
        ETLActionPattern pattern = new ETLActionPattern();
        ETLCheck check = new ETLCheck();
        check.setName("Проверка №1");
        check.setQuery("SELECT 1 ");
        check.setTarget(TARGETS.MSSQL);
        check.setOperator(OPERATORS.EQUALS);
        check.setRightValue("1");
        pattern.getChecks().add(check);
        config.getPatterns().add(pattern);
        XMLHelper.writeXML("C:/usr/etl/test.xml", config,true, ETLConfig.class, ETLActionPattern.class, ETLCheck.class);
    }
}
