package ru.sberbank.syncserver2.service.generator.single.data;

import java.util.Date;

public class ActionState {
	
	// Список фаз генерации файла
	public static final String PHASE_DB_CONNECT = "DB_CONNECT";
	public static final String PHASE_FILE_GENERATING = "FILE_GEN";
	public static final String PHASE_SENDING_TO_SIGMA = "SEND_TO_SIGMA";
	public static final String PHASE_LOADING_TO_SIGMA = "LOADED_TO_SIGMA";
	
	// список статусов фазы генерации файла
	public static final String STATUS_PERFORM = "PERFORM";
	public static final String STATUS_COMPLETED_ERROR = "COMPLETED_ERROR";
	public static final String STATUS_COMPLETED_SUCCESSFULLY = "COMPLETED_SUCCESSFULLY";
	public static final String STATUS_CANCELED_BY_USER = "CANCELED_BY_USER";
	
	// Название файлов-картинок сигналов
	public static final String IMAGE_FILE_NAME_GREEN = "green.png";
	public static final String IMAGE_FILE_NAME_RED = "red.png";
	public static final String IMAGE_FILE_NAME_YELLOW = "yellow.png";
	public static final String IMAGE_FILE_NAME_BLACK = "black.png";
	public static final String IMAGE_FILE_NAME_STOP_HAND = "stop-hand.png";
	
	
	/**
	 * Общий список альфа-фаз генерации файла
	 */
	private static String[] alphaPhases = {ActionState.PHASE_DB_CONNECT,ActionState.PHASE_FILE_GENERATING,ActionState.PHASE_SENDING_TO_SIGMA};
	
	private String statusCode;
	private String statusName;
	
	private String phaseCode;
	private String phaseName;
	
	private String fileName;
	private String sigmaHost;
	private String webHostName;
	
	private Date eventDate;
	
	public ActionState() {
		super();
	}

	public ActionState(String statusCode, String statusName, String phaseCode,
			String phaseName, String fileName, String sigmaHost,Date eventDate,String webHostName) {
		super();
		this.statusCode = statusCode;
		this.statusName = statusName;
		this.phaseCode = phaseCode;
		this.phaseName = phaseName;
		this.fileName = fileName;
		this.sigmaHost = sigmaHost;
		this.eventDate = eventDate;
		this.webHostName = webHostName;
	}
	
	public static String[] getAllAlphaStates() {
		return alphaPhases;
	}

	/**
	 * Получить название файла картинки сигнала в зависимости от его статуса
	 * @return
	 */
	public String getSignalColorFileName() {
		if (ActionState.STATUS_PERFORM.equals(statusCode))
			return IMAGE_FILE_NAME_YELLOW;
		else if (ActionState.STATUS_COMPLETED_SUCCESSFULLY.equals(statusCode))
			return IMAGE_FILE_NAME_GREEN;
		else if (ActionState.STATUS_COMPLETED_ERROR.equals(statusCode))
			return IMAGE_FILE_NAME_RED;
		else if (ActionState.STATUS_CANCELED_BY_USER.equals(statusCode))
			return IMAGE_FILE_NAME_STOP_HAND;
		else 
			return IMAGE_FILE_NAME_BLACK;	
	}
	
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusName() {
		return statusName;
	}
	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}
	public String getPhaseCode() {
		return phaseCode;
	}
	public void setPhaseCode(String phaseCode) {
		this.phaseCode = phaseCode;
	}
	public String getPhaseName() {
		return phaseName;
	}
	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSigmaHost() {
		return sigmaHost;
	}
	public void setSigmaHost(String sigmaHost) {
		this.sigmaHost = sigmaHost;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public String getWebHostName() {
		return webHostName;
	}

	public void setWebHostName(String webHostName) {
		this.webHostName = webHostName;
	}
	
}
