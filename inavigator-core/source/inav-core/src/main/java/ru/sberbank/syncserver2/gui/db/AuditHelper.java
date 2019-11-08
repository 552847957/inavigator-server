package ru.sberbank.syncserver2.gui.db;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import ru.sberbank.syncserver2.gui.data.AuditRecord;
import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.ClusterHookProvider;

@Service
public class AuditHelper {
	
	private static final Logger LOGGER = Logger.getLogger(AuditHelper.class);	
	protected static DatabaseManager database;// = new DatabaseManager(ServiceManager.getInstance().getConfigSource());
	private static JdbcTemplate jdbcTemplate;// = new JdbcTemplate(ServiceManager.getInstance().getConfigSource());
	
	@Autowired
	public void setDatabase(DatabaseManager database) {
		AuditHelper.database = database;
		AuditHelper.jdbcTemplate = new JdbcTemplate(database.getConfigSource());
	}

	private static final String SQL = "exec SP_SYNC_AUDIT_LOGMSG ?, ?, ?, ?, ?, ?, ?";
	
	public static final int LOGIN = 1;
	public static final int CONFIGURATION = 2;
	public static final int EMPLOYEE = 3;
	public static final int SERVICE = 4;
	public static final int SYSTEM_INFO = 5;
	public static final int CLIENT_PROP = 6;
	public static final int AUDIT = 7;
	public static final int GENERATION = 8;
	public static final int MOBILE_MODES = 9;
	public static final int PUSH_NOTIFICATION = 10;
	public static final int CONTENT_DRAFT_GROUP = 11;	
	
    public final static String LOCAL_HOST_NAME;
    static {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            if (ClusterHookProvider.isClusterHooked()) {
            	hostName += "_"+ClusterHookProvider.getSuffixForHook();
            }
        } catch (UnknownHostException e) {
        }
        LOCAL_HOST_NAME = hostName;
    }
	
	public static boolean isValidUser(String email, String password) {
		return database.authenticate(email, password).getStatus()==AuthContext.VALID_USER;
	}
	
	public static List<AuditRecord> getRecords(String where, String order, int startIndex, int numberOfMessages) {
		//without description, IP and code
		String sql = "SELECT * FROM (SELECT [RANK] = ROW_NUMBER() OVER ("+order+"), "+ 
			    "* FROM SYNC_AUDIT "+where+" ) A WHERE A.[RANK] BETWEEN "+(startIndex+1)+" AND "+(startIndex+numberOfMessages);
		List<AuditRecord> list = jdbcTemplate.query(sql, new RowMapper<AuditRecord>() {
			@Override
			public AuditRecord mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new AuditRecord(arg0);
			}
			
		});
		return list;
	}
	
	public static List<CompleteAuditRecord> getCompleteRecords(String where, String order, boolean decorateGenerationMsgs) {
		
		List<CompleteAuditRecord> list = database.list(CompleteAuditRecord.class, where+order);
		if (decorateGenerationMsgs)
			for (CompleteAuditRecord record: list) {
				if (record.getCode()==GENERATION) {	
					String fileName = record.getDescription();
					int id = record.getEventId();
					String sql = "SELECT TOP 1 EVENT_TIME FROM SYNC_LOGS WHERE EVENT_TYPE='"+LogEventType.GEN_QUEUED+"' AND EVENT_INFO='"+fileName+"' AND EVENT_ID>(SELECT TOP 1 EVENT_ID FROM SYNC_LOGS WHERE EVENT_TYPE='"+LogEventType.OTHER+"' AND EVENT_INFO='"+fileName+"' AND EVENT_DESC LIKE '%audit%id "+id+"%') ORDER BY EVENT_ID ASC";
					Date date = database.getDateValue(sql);
					if (date!=null) {
						String newDesc = "<a href='logs.generator.gui?dataFileName="+fileName+"&date="+JSPFormatPool.formatDateAndTime2(date.getTime())+"'>"+fileName+"</a>";					
						record.setDescription(newDesc);
					}				
				}			
			}
		return list;
	}
	
	public static void deleteRecords(String where) {
		String sql = "DELETE FROM SYNC_AUDIT " + where;
		database.execute(sql, new Object[0]);		
	}
	
	public static int[] getSizeOfTable(String where) {
		return jdbcTemplate.query("select (select count(*) from SYNC_AUDIT) as a, (select count(*) from SYNC_AUDIT "+where+") as b", new ResultSetExtractor<int[]>() {
			@Override
			public int[] extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				rs.next();
				return new int[] {rs.getInt("a"),rs.getInt("b")};
			}
		});
	}
	
	public static int write(HttpServletRequest request, String eventType, String description, int code) {
		return write(request.getRemoteAddr(), getEmail(request), getModule(request), eventType, description, code);
	}
	
	
	public static int writeSystemGen(String eventType, String eventDescription) {
		return write(null, "SYSTEM", "generator", eventType, eventDescription, GENERATION);
	}	
	
	public static int write(String IP, String email, String module, String eventType, String description, int code) {
		try {
			return jdbcTemplate.queryForInt(SQL, IP, email, module, eventType, description, code, LOCAL_HOST_NAME);
		} catch (Throwable th) {
			LOGGER.error("Can't write audit log", th);
			return -1;
		}
	}
	
	public static String composeEditMsg(String field, String old, String cur) {
		if (field.toLowerCase().contains("password"))
			return "Изменение значения поля "+field;
		return "Изменение значения поля "+field+" со значения "+old+" на значение "+cur;
	}	

	
	private static AuthContext getAuthContext(HttpServletRequest request) {
		return (AuthContext) request.getSession().getAttribute("user");
	}

	private static Employee getEmployee(HttpServletRequest request) {
		AuthContext user = getAuthContext(request);
		Employee employee = user == null ? null : user.getEmployee();
		return employee;
	}
	
	private static String getEmail(HttpServletRequest request) {
		Employee employee = getEmployee(request);		
		return employee==null?null:employee.getEmployeeEmail(); 
	}
	
	private static String getModule(HttpServletRequest request) {
		return request.getServletContext().getInitParameter("webAppRootKey");
	}

}
