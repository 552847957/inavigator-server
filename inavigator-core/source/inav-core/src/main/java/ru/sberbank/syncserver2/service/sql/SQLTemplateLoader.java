package ru.sberbank.syncserver2.service.sql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.AbstractConfigLoader;
import ru.sberbank.syncserver2.service.sql.query.TextSubstitute;
import ru.sberbank.syncserver2.util.constants.INavConstants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sbt-kozhinsky-lb on 11.03.14.
 */
public class SQLTemplateLoader extends SingleThreadBackgroundService {
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String,String> templates = new HashMap<String, String>();
    private Map<String,String> substitutions = new HashMap<String, String>();
    
    private JdbcTemplate jdbcTemplate = null;
    
    public SQLTemplateLoader() {
        super(60); //10 seconds to waite between executions
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        /**
         * We should load all templates here since this function is called in MSSQLService startup
         * Calling load here instead of doInit, we make this service independent from startup order
         */
        loadAll();
    }

    protected void loadAll(){
        //1. Check if jdbc template was already initialized
        if(jdbcTemplate==null){
            return;
        }
        
        AbstractConfigLoader configLoader = null;
        // обновляем настройки конфига
        try {
        	configLoader = (AbstractConfigLoader)getServiceContainer().getServiceManager().getConfigLoader();
        	configLoader.updateMacros();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        //2. Loading
        Map localMap = new HashMap<String,String>();
        Map localMap2 = new HashMap<String,String>();
        try {
            List<Template> localTemplates = jdbcTemplate.query(INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_SQL_SQLTEMPLATELOADER_LOADALL1, new RowMapper<Template>() {
                @Override
                public Template mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Template(rs.getString("TEMPLATE_CODE"),rs.getString("TEMPLATE_SQL"));
                }
            });
            
            List<SubstDict> localSubstitutes = jdbcTemplate.query(INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_SQL_SQLTEMPLATELOADER_LOADALL2, new RowMapper<SubstDict>() {
                @Override
                public SubstDict mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new SubstDict(rs.getString("SUBST_CODE"),rs.getString("SUBST_VALUE"));
                }
            });

            for(SubstDict dict:localSubstitutes) {
            	localMap2.put(dict.getSubstCode(), dict.getSubstValue());
            }
            
            for (int i = 0; i < localTemplates.size(); i++) {
                Template t = (Template) localTemplates.get(i);
                
                // Применяем конфиг-макросы к строке с шаблоном
                if (configLoader != null)
                	t.setTemplateSQL(configLoader.applyMacrosToString(t.getTemplateSQL()));
                
                localMap.put(t.getTemplateCode(), t.getTemplateSQL());
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        //3. Replacing
        try {
            lock.writeLock().lock();
            templates = localMap;
            substitutions = localMap2;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Произвести замену всех переменных-подстановок для входной строки
     * @param s
     * @param textSubstitutions
     * @return
     */
    public String executeTextSubstitution(String s,List<TextSubstitute> textSubstitutions) {
    	if (textSubstitutions == null)
    		return s;
    	
        for(TextSubstitute textSubstitute:textSubstitutions) {
        	if (substitutions.containsKey(textSubstitute.getSubstituteCode()))
        		s = s.replaceAll("\\$\\{" + textSubstitute.getSubstituteVarName() + "\\}",substitutions.get(textSubstitute.getSubstituteCode()));
        }
        return s;
    }
    
    public String getTemplateSQL(String templateCode){
        try {
            lock.readLock().lock();
            String sql = templates.get(templateCode);
            return sql;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    @Override
    public void doInit() {
        //taking binding into account, the initialization should be in <code>setJdbcTemplate</code>
    }

    @Override
    public void doRun() {
        loadAll();
    }
    
    public static class SubstDict {
    	private String substCode;
    	private String substValue;

		public SubstDict(String substCode, String substValue) {
			super();
			this.substCode = substCode;
			this.substValue = substValue;
		}
    	
    	public String getSubstCode() {
			return substCode;
		}
		public void setSubstCode(String substCode) {
			this.substCode = substCode;
		}
		public String getSubstValue() {
			return substValue;
		}
		public void setSubstValue(String substValue) {
			this.substValue = substValue;
		}
    }
    

    public static class Template {
        private String templateCode;
        private String templateSQL;

        public Template(String templateCode, String templateSQL) {
            this.templateCode = templateCode;
            this.templateSQL = templateSQL;
        }

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public String getTemplateSQL() {
            return templateSQL;
        }

        public void setTemplateSQL(String templateSQL) {
            this.templateSQL = templateSQL;
        }
    }
}

