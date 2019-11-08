package ru.sberbank.syncserver2.service.file.fragments;

/**
 * Возможные результаты операций при работе с фрагментами файлов
 * @author sbt-gordienko-mv
 *
 */
public class FileFragmentOperationResult {
	
	/**
	 * Тип результата
	 */
	private FileFragmentOperationResultTypes resultType;
	
	/**
	 * Имя результирующего файла 
	 */
	private String fileName;

	public FileFragmentOperationResult(FileFragmentOperationResultTypes resultType) {
		this.resultType = resultType;
	}
	
	public FileFragmentOperationResult(FileFragmentOperationResultTypes resultType,String fileName) {
		this.resultType = resultType;
		this.fileName = fileName;
	}

	
	public FileFragmentOperationResultTypes getResultType() {
		return resultType;
	}

	public void setResultType(FileFragmentOperationResultTypes resultType) {
		this.resultType = resultType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	
}
