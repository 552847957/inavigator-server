package ru.sberbank.syncserver2.service.file.fragments;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import ru.sberbank.syncserver2.util.FileCopyHelper;

/**
 * Информация о файле, который был разархивирован
 * @author sbt-gordienko-mv
 *
 */
class InflatedFileInfo {
	
	/* Поля файла метаданных который пересылается в ZIP архиве с фрагментом */
	public static final String METADATA_PROPERTY_FRAGMENT_COUNT = "fragmentCount";
	public static final String METADATA_PROPERTY_CURRENT_FRAGMENT_NUMBER = "currentFragmentNumber";
	public static final String METADATA_PROPERTY_FILE_GUID = "fileGuid";
	public static final String METADATA_PROPERTY_WHOLE_FILE_MF5 = "wholeFileMd5";
	public static final String METADATA_PROPERTY_FILE_GEN_TIMESTAMP = "fileGenTimestamp"	;
	public static final String METADATA_PROPERTY_FILENAME_FIELD_NAME = "fileName";
	public static final String METADATA_PROPERTY_FILE_CREATE_DATE_FIELD_NAME = "fileCreateDate";
	public static final String METADATA_PROPERTY_MD5_FIELD_NAME = "md5";
	
	/* формат даты в файле метаданных */
	public static final String METADATA_PROPERTY_DATE_PATTERN = "dd.MM.yyyy HH:mm:ss";
	
	
	/**
	 * Имя файла
	 */
	private String fileName;
	
	/**
	 * MD5 файла
	 */
	private String md5;
	
	/**
	 * Дата создания файла в alpha
	 */
	private String fileCreateDate;
	
	/**
	 * Ссылка на File - с данными файла
	 */
	private File fileBinaryContentTemp;
	
	/**
	 * Дополнительный данные идущие вместе с файлом
	 */
	private Properties additionalProperties;
	
	
	/**
	 * Получить количество фрагментов
	 * @return
	 */
	public int getFragmentCount() {
		if (getAdditionalProperties() != null && getAdditionalProperties().getProperty(METADATA_PROPERTY_FRAGMENT_COUNT) != null)
			return Integer.valueOf(getAdditionalProperties().getProperty(METADATA_PROPERTY_FRAGMENT_COUNT));
		else
			return 0;
	}
	
	/**
	 * Удален содержимого файла с диска
	 */
	public void clearBinaryContent() {
		FileCopyHelper.reliableDelete(fileBinaryContentTemp);
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getFileCreateDate() {
		return fileCreateDate;
	}
	public void setFileCreateDate(String fileCreateDate) {
		this.fileCreateDate = fileCreateDate;
	}
	
	public File getFileBinaryContentTemp() {
		return fileBinaryContentTemp;
	}
	public void setFileBinaryContentTemp(File fileBinaryContentTemp) {
		this.fileBinaryContentTemp = fileBinaryContentTemp;
	}
	
	public Properties getAdditionalProperties() {
		return additionalProperties;
	}
	public void setAdditionalProperties(Properties additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
	
}
