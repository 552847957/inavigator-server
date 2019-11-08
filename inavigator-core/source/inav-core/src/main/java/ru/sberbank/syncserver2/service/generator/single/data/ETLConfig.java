package ru.sberbank.syncserver2.service.generator.single.data;

import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 27.01.12
 * Time: 19:30
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "etl-config",namespace = "")
public class    ETLConfig {
    private ArrayList<ETLAction>          actions  = new ArrayList<ETLAction>();
    private ArrayList<ETLActionPattern>   patterns = new ArrayList<ETLActionPattern>();

    @XmlElement(name = "etl-action")
    public ArrayList<ETLAction> getActions() {
        return actions;
    }

    public void setActions(ArrayList<ETLAction> actions) {
        this.actions = actions;
    }

    @XmlElement(name = "etl-action-pattern")
    public ArrayList<ETLActionPattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(ArrayList<ETLActionPattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public String toString() {
        return "ETLConfig{" +
                "actions=" + actions +
                ", patterns=" + patterns +
                '}';
    }

    public static ETLConfig loadConfiFile(String path){
        //1. Reading file
        ETLConfig config = (ETLConfig) XMLHelper.readXML(path, ETLConfig.class, ETLDatabase.class, ETLAction.class, ETLActionPattern.class, ETLActionChangeType.class,ETLSeriesName.class);
        if (config == null) {
            throw new IllegalArgumentException("cannot parse etl config file: " + path);
        }
        //2. Mapping patterns to actions
        //2.1. Buidling pattern map
        Map patterns = new HashMap();
        for (int i = 0; i < config.patterns.size(); i++) {
            //2.1.1. Check for dublicates
            ETLActionPattern pattern =  config.patterns.get(i);
            if(patterns.containsKey(pattern.getPatternName())){
                try {
                    throw new RuntimeException("Dublicate found for pattern "+pattern.getPatternName()+" in file "+path);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }

            //2.1.2. Buidling pattern map
            patterns.put(pattern.getPatternName(), pattern);
        }

        //2.2. Mapping patterns to actions
        Set unique = new TreeSet();
        for (int i = 0; i < config.actions.size(); i++) {
            //2.2.1. Check for dublicates
            ETLAction action =  config.actions.get(i);
            action.setConfigFileName(path);
            if(!unique.add(action.getDataFileName())){
                try {
                    throw new RuntimeException("Dublicate found for action with datafile "+action.getDataFileName()+" in file "+path);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }

            //2.2.2. Mapping pattern to action
            String patternName = action.getPatternName();
            ETLActionPattern pattern = (ETLActionPattern) patterns.get(patternName);
            if(pattern==null){
                try {
                    throw new RuntimeException("No pattern "+patternName+" found for action with datafile "+action.getDataFileName()+" in file "+path);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            action.setPatternObject(pattern);
        }
        return  config;
    }

    public static void main(String[] args) throws SQLException {
        File[] files = new File("C:\\usr\\temp\\").listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println("Start reading "+file.getAbsolutePath());
            ETLConfig config = ETLConfig.loadConfiFile(file.getAbsolutePath());
            System.out.println("Finish reading "+file.getAbsolutePath());
        }
        //System.out.println("Config = "+config);
    }

}
