package ru.sbt.utils.backup.util;

import java.io.*;
import java.util.Date;

import ru.sbt.utils.backup.command.AbstractCommand;
import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.configuration.TableInfo;
import ru.sbt.utils.backup.configuration.XmlConfiguration;

public class Logger {
	
	private static Logger logger = null;
	private static String currentTimestamp = null;

	private AbstractCommand command = null;
	private XmlConfiguration configuration = null;
	
	private int errorCount = 0;
	private int allCount = 0;
	private PrintStream printStream;
	private static final String CHARSET = "cp1251";

	private Logger() {
		errorCount = 0;
		allCount = 0;
	}

	private static String getCurrentTimeStamp() {
		if (currentTimestamp == null)
			currentTimestamp = DateUtils.getFormattedDate(new Date(), "yyyy.MM.dd.HH.mm.ss");
		return currentTimestamp;
	}
	
	/**
	 * Проинициализировать логгер для текущего сеанса вызова утилиты
	 * @param comm
	 * @param config
	 */
	public static void init(AbstractCommand comm,XmlConfiguration config) throws FileNotFoundException, UnsupportedEncodingException {
		if (logger != null) {
			logger.errorCount = 0;
			logger.allCount = 0;
			return;
		}
		logger = new Logger();
		logger.command = comm;
		logger.configuration = config;
		
		// создаем корневую папку для логов
		File rootFolder = new File(config.getLogsRoot());
		if (!rootFolder.exists()) {
			if (!rootFolder.mkdirs()) {
				throw new FileNotFoundException("Не удалось создать директорию для логов " + rootFolder.getAbsolutePath());
			}
		}
		logger.printStream = new PrintStream(config.getLogsRoot() + File.separator + getCurrentTimeStamp() + "_" + comm.getCommandName() + ".log", CHARSET);
	}
	
	/**
	 * Добавить в лог запись с результатом успешно
	 * @param message
	 */
	public void success(String message) {
		log("SUCCESS",message);
		allCount++;
	}

	/**
	 * Добавить в лог инфорамционную запись
	 * @param message
	 */
	public void info(String message) {
		log("INFO",message);
	}

	/**
	 * Добавить в лог запись с результатом ошибка
	 * @param message
	 */
	public void error(String message) {
		log("ERROR",message);
		allCount++;
		errorCount++;
	}
	
	/**
	 * Добавить запиьс лога с укзаанием результата
	 * @param result
	 * @param message
	 */
	public void log(String result,String message) {
		String logStr = DateUtils.getFormattedDate(new Date(), "dd.MM.yyyy HH:mm:ss") + "|" + command.getCommandName() + "|" + result + "|" + message;
		printStream.println(logStr);
		System.out.println(logStr);
	}
	
	/**
	 * получить инстанс логгера
	 * @return
	 */
	public static Logger getInstance() {
		if (logger != null)
			return logger;
		else
			throw new RuntimeException("Logger doesn't initialized properly!");
	}

	public String getStaticticalResult() {
		return "[Ошибок " + errorCount + " из " + allCount + " шагов]";
	}

	public int getErrorCount() {
		return errorCount;
	}

	public static String createFullSqlStringName(DatabaseServerInfo dbsinfo, DatabaseInfo dbinfo, TableInfo tinfo) {
		return 
				((dbsinfo != null)?( "[" + dbsinfo.getHost() + "]."):"") + 
				((dbinfo != null)?( "[" + dbinfo.getName() + "]."):"") + 
				((tinfo != null)?("[" + tinfo.getName() + "]"):""); 
				
	}
	public static String createFullSqlStringName(DatabaseServerInfo dbsinfo,DatabaseInfo dbinfo) {
		return 
				((dbsinfo != null)?( "[" + dbsinfo.getHost() + "]."):"") + 
				((dbinfo != null)?( "[" + dbinfo.getName() + "]"):"");
				
	}

	public void close() {
		try {
			printStream.close();
		} catch (Exception e) {
			//ignore
		}
	}

	public void printStackTrace(Throwable th) {
		th.printStackTrace(printStream);
	}

	public void printAdditionalInfo(String msg) {
		printStream.println(msg);
	}
}
