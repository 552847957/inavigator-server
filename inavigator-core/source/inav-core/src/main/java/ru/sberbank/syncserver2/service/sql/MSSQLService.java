package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequestDBExecutor;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sbt-kozhinsky-lb on 03.03.14.
 */
public class MSSQLService extends BackgroundService implements SQLService,  Dispatchable {
	private static final String DRIVER_CLASSNAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private String mssqlURL;
	private String mssqlUser;
	private String mssqlPassword;
    private String serviceName;
    private String driverClassName;
    private int timeout = 120;
    private int maxIdle = 8;	// максимальное количество простаиваемых потоков в пуле
    private int maxActive = 16;	// максимальное количество потоков в пуле
    private long maxWait = -1;	// максимальное время (ms) ожидания выделения thread-а из пула потоков для обработки запроса, по истечении будет брошено исключение
    private boolean poolingStatements = false;
    private int maxOpenStatements = -1;	// максимальное количество PrepearedStatements в пуле (если poolingStatements = true)
    private int minIdle = 0; 	// минимальное количество потоков, которое должно простаивать, чтобы не создавались новые потоки
	private boolean forcedToTemplateUsage = false; // принудительное использование шаблона

    private BasicDataSource dataSource;

    private AtomicBoolean restarted;

	private OnlineRequestDBExecutor processor;
    private SQLTemplateLoader templateLoader;
	private JdbcTemplate jdbcTemplate;

	public String getMssqlURL() {
        return mssqlURL;
    }

    public void setMssqlURL(String mssqlURL) {
        this.mssqlURL = mssqlURL;
    }

    public String getMssqlUser() {
		return mssqlUser;
	}

	public void setMssqlUser(String mssqlUser) {
		this.mssqlUser = mssqlUser;
	}

	public String getMssqlPassword() {
		return mssqlPassword;
	}

	public void setMssqlPassword(String mssqlPassword) {
		this.mssqlPassword = mssqlPassword;
	}

    @Override
    public boolean isRestarted() {
        return restarted.getAndSet(false);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public SQLTemplateLoader getTemplateLoader() {
        return templateLoader;
    }

    public void setTemplateLoader(SQLTemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
    }



    public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	private List<String> localErrors = new ArrayList<String>();

	public void setDataSource(BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setProcessor(OnlineRequestDBExecutor processor) {
		this.processor = processor;
	}

	@Override
    protected void doStop() {
		logServiceMessage(LogEventType.SERV_STOP, "stopping service");
		logger.info("Stop MSSQLService...");

		if (dataSource != null)
			try {
				dataSource.close();
			} catch (SQLException e) {
				logger.error(e, e);
				tagLogger.log("Ошибка при закрытии пула потоков: "+e.toString());
			}
    	processor = null;
    	localErrors.clear();

    	logger.info("Stop MSSQLService finished");
    	logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    public void doStart() {
    	logServiceMessage(LogEventType.SERV_START, "starting service");
    	Properties props = new Properties();
        props.put("driverClassName", DRIVER_CLASSNAME);
        props.put("url", mssqlURL);
        props.put("username", mssqlUser);
        props.put("password", mssqlPassword);
    	logger.info("Start MSSQLService with properties: " + props);

    	for (String s: localErrors) {
    		tagLogger.log(s);
    	}

    	try {
    		 dataSource = new BasicDataSource(); // не забыть вызвать close()
    		 dataSource.setDriverClassName((driverClassName == null || driverClassName.equals(""))?DRIVER_CLASSNAME:driverClassName);
    		 dataSource.setUrl(mssqlURL);
    		 dataSource.setUsername(mssqlUser);
    		 dataSource.setPassword(mssqlPassword);
    		 dataSource.setMaxWait(maxWait); // максимальное время (ms) ожидания выделения thread-а из пула для обработки запроса, иначе будет брошено исключение
    		 dataSource.setMaxActive(maxActive);
    		 dataSource.setMaxIdle(maxIdle); // максимальное количество простаиваемых потоков в пуле
    		 dataSource.setPoolPreparedStatements(poolingStatements);
    		 dataSource.setMaxOpenPreparedStatements(maxOpenStatements);
    		 dataSource.setMinIdle(minIdle);

    		 //DataSource dataSource = new DriverManagerDataSource((driverClassName == null || driverClassName.equals(""))?DRIVER_CLASSNAME:driverClassName, mssqlURL, mssqlUser, mssqlPassword);
			 jdbcTemplate = new JdbcTemplate(dataSource);
    		 jdbcTemplate.setQueryTimeout(timeout);

    		 processor = new OnlineRequestDBExecutor(jdbcTemplate);

            if(templateLoader!=null){
                templateLoader.setJdbcTemplate(new JdbcTemplate(super.getServiceContainer().getServiceManager().getConfigSource()));
            }
    	} catch (Throwable th) {
    		logger.error("Start MSSQLService ERROR ", th);
            logError(LogEventType.SERV_START, "exception during starting "+serviceBeanCode, th);
    		throw new RuntimeException(th);
    	}

        restarted = new AtomicBoolean(true);
    	logger.info("Start MSSQLService finished");
    	logServiceMessage(LogEventType.SERV_START, "started service");

    }

    private Properties composeMSSQLProperties() {
        Properties props = new Properties();
        props.put("driverClassName","com.microsoft.sqlserver.jdbc.SQLServerDriver");
        props.put("url","jdbc:sqlserver://10.21.25.55:1433;databaseName=syncserver20");
        props.put("username","syncuser");
        props.put("password","123456");
        return props;
    }

    @Override
    public DataResponse request(OnlineRequest request) {
        ExecutionTimeProfiler.start("MSSQLService.request");
        try {
            if (processor == null) {
                return null;
            }

            if(templateLoader!=null){
                String code = request.getStoredProcedure();
                String sql  = templateLoader.getTemplateSQL(code);
                if(sql!=null){
                    request.setStoredProcedure(sql);
                } else if (forcedToTemplateUsage){
                	DataResponse resp = new DataResponse();
                	resp.setError("There is no template with name "+code);
                	resp.setResult(DataResponse.Result.FAIL_ACCESS);
                	return resp;
				}

                // применяем подстановку к результату запроса
                request.setStoredProcedure(templateLoader.executeTextSubstitution(request.getStoredProcedure(),request.getTextSubstitutes()!= null?request.getTextSubstitutes().getTextSubstitute():null));
            }

            return processor.query(request);
        } finally {
            ExecutionTimeProfiler.finish("MSSQLService.request");
        }
    }

	public void setTimeout(String timeout) {
		try {
			this.timeout = Integer.parseInt(timeout);
		 } catch (Exception e) {
			 localErrors.add("Ошибка при попытке выставить timeout:"+e.toString());
		 }
	}

	public void setMaxIdle(String maxIdle) {
		try {
			this.maxIdle = Integer.parseInt(maxIdle);
		 } catch (Exception e) {
			 localErrors.add("Ошибка при попытке выставить maxIdle:"+e.toString());
		 }
	}

	public void setMaxActive(String maxActive) {
		try {
			this.maxActive = Integer.parseInt(maxActive);
		 } catch (Exception e) {
			localErrors.add("Ошибка при попытке выставить maxActive:"+e.toString());
		 }
	}

	public void setMaxWait(String maxWait) {
		try {
			this.maxWait = Long.parseLong(maxWait);
		 } catch (Exception e) {
			localErrors.add("Ошибка при попытке выставить maxWait:"+e.toString());
		 }
	}

	public void setPoolingStatements(String poolingStatements) {
		try {
			this.poolingStatements = Boolean.parseBoolean(poolingStatements);
		 } catch (Exception e) {
			localErrors.add("Ошибка при попытке выставить poolingStatements:"+e.toString());
		 }
	}

	public void setMaxOpenStatements(String maxOpenStatements) {
		try {
			this.maxOpenStatements = Integer.parseInt(maxOpenStatements);
		 } catch (Exception e) {
			localErrors.add("Ошибка при попытке выставить maxOpenStatements:"+e.toString());
		 }
	}

	public void setMinIdle(String minIdle) {
		try {
			this.minIdle = Integer.parseInt(minIdle);
		 } catch (Exception e) {
			localErrors.add("Ошибка при попытке выставить minIdle:"+e.toString());
		 }
	}

	public void setForcedToTemplateUsage(String forcedToTemplateUsage) {
		try {
			this.forcedToTemplateUsage = Boolean.parseBoolean(forcedToTemplateUsage);
		} catch (Exception e) {
			localErrors.add("Ошибка при попытке выставить forcedToTemplateUsage:"+e.toString());
		}
	}

	/*
    public static void main(String[] args) {
        //1. Creating service
        SQLiteService service = new SQLiteService();
        service.setLocalIncomingFile("C:\\usr\\cache\\dev\\syncserver\\sqliteInbox\\phonebook.sqlite");
        service.setLocalWorkFolder("C:\\usr\\cache\\dev\\syncserver\\sqliteWork\\");
        service.doInit();
        service.doRun();

        //2. Creating requiest
        OnlineRequest or = new OnlineRequest();
        or.setStoredProcedure("SELECT * FROM 'phonebook.sectors'");
        or.setProvider("SQLITE");
        or.setService("finik1");

        //3. Runing onlune request
        System.out.println("REQUEST: "+or);
        DataResponse dr = service.request(or);
        System.out.println("RESULT: " + dr);
    }

    public static void main(String[] args) {
        String mssqlURL = "jdbc:sqlserver://10.21.25.55:1433;databaseName=syncserver20;username=syncuser;password=123456";

    }

    @Override
    public DataResponse request(OnlineRequest request) {
        return processor.query(request);
    } */
}
