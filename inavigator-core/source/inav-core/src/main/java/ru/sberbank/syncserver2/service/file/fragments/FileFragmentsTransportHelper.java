package ru.sberbank.syncserver2.service.file.fragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;

import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

/**
 * Данный класс содержит все методы необхоидимые для 
 * 1. Разбиения входного файла на фрагменты
 * 2. Промежуточных шагов и финальной склейки файла
 * 3. Дополнительные методы, вызываемые в сервисах для поддержки механизма передачи файлов по частям
 * @author sbt-gordienko-mv
 *
 */
public class FileFragmentsTransportHelper {
	private static final int MAX_COUNT_ARHIVE_FILES = 5;
	private static final String ZIP_METADATA_FILE_NAME = "metadata";
	private static final String FILECOPY_LOK_EXT = ".FILECOPY.LOK";
	private static final String FRAGMENT_ARCHIVE_FILE_EXTENSION = "fzip";
	private static final int MAX_WRITE_BUFFER_SIZE = 1024*1024*50;
	
	private static TagLogger tagLogger =  TagLogger.getTagLogger(FileFragmentsTransportHelper.class);
	
	/**
	 * Является ли файл  - архивом файла фрагмента
	 * (В данный момент проверяется по наличию заранее оговоренного расширения файла)
	 * @param file
	 * @return
	 */
	public static boolean isFileFragment(File file) {
		if (file == null) return false;
		return file.getName().endsWith(FileFragmentsTransportHelper.FRAGMENT_ARCHIVE_FILE_EXTENSION);
	}
	
	/**
	 * Определить существует ли файл-блокировка 
	 * @param inputFile
	 * @param archiveFolder
	 * @return
	 */
	public static boolean isLockFileExists(File archiveFolder,String filename) {
		File f = new File(archiveFolder,getSourceFileNameFromFragmentFile(filename) + FILECOPY_LOK_EXT);
		return f.exists();
	}
	
	/**
	 * Получить список фрагментов файлов
	 * @param archiveFolder
	 * @return
	 */
	public static File[] getAllFragments(File archiveFolder) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !ZIP_METADATA_FILE_NAME.equals(name);
			}
		};
		File fileFragments[] = archiveFolder.listFiles(filter);
		return fileFragments; 
	}
	
	/**
	 * Добавить фрагмент Файла в папку-архив
	 * @param inputFile
	 * @param archiveFolder
	 * @return
	 */
	public static FileFragmentOperationResultTypes addedFileFragmentToArchive(File inputFile,File archiveFolder) {
		
		try {
			// вместо архивной папки используем папку с именем файла
			archiveFolder = new File(archiveFolder,FileFragmentsTransportHelper.getSourceFileNameFromFragmentFile(inputFile.getName()));
			
			// если существует файл в архиве ( от старой версии) а не папка - то удаляем данный файл
			if (archiveFolder.exists() && !archiveFolder.isDirectory())
				FileCopyHelper.reliableDelete(archiveFolder);
			
			// если папка с именем файла не существует - создаем ее
			FileHelper.createMissingFolders(archiveFolder.getAbsolutePath());
			
			// получаем данные из файла с архивом фрагмента
			InflatedFileInfo fileInfo = getInflatedFile(inputFile,archiveFolder);
			// в данном случае содержимое файл не требуется, поэтому удаляем его сразу
			fileInfo.clearBinaryContent();
			
//			File fileFragmentsFolder = new File(archiveFolder,fileInfo.getAdditionalProperties().getProperty(InflatedFileInfo.METADATA_PROPERTY_FILE_GUID));
//			// создаем подпапку с названием-гуидом файла
//			FileHelper.createMissingFolders(fileFragmentsFolder.getAbsolutePath());
			
			// обновляем временную папку сравнивая гуиды и даты генерации файлов
			FileFragmentOperationResultTypes resultType =  updateTempFolderByCompareNewFileFragmentMetadataWithOldCollected(archiveFolder,fileInfo,true);
			
			// если результат предыдущей операции - игнорирование фрагмента, то выходим с аналогичным результатом
			if (resultType.equals(FileFragmentOperationResultTypes.OPERATION_IGNORED))
				return resultType;
			
			// копируем файл в архивную папку
			FileCopyHelper.reliableCopy(inputFile, new File(archiveFolder,inputFile.getName()));
			
			// проверяем собраны ли все фрагменты, чтобы удалить блокировку на повторное копирование
			if (checkFragmentCount(archiveFolder, fileInfo.getFragmentCount(),true))
				updateFileLock(inputFile, archiveFolder.getParentFile(), false);

//			// удаляем лишние файлы
//			clearOldArchiveFiles(archiveFolder);
			
			return FileFragmentOperationResultTypes.OPERATION_OK;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return FileFragmentOperationResultTypes.OPERATION_ERROR;
		}
	}
	
	/**
	 * Скопировать фрагменты файлов на файлоперекладчик 
	 * @param src
	 * @param tmp
	 * @param dst
	 */
	public static void copyFileFragmentToNetworkFolder(File archiveFile,File networkTmp,File networkDst) {
		// ищем последний сгенерированный файл в папке файлов (с наибольшей lastModifiedDate)
//		File[] files = archiveFile.listFiles();
//		File lastGeneratedFile = Collections.max(Arrays.asList(files),new Comparator<File>() {
//			@Override
//			public int compare(File f1, File f2) {
//				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
//			}
//		});
	
		// копируем данные из найденной папки с фрагментами в сетевую папку
		File[] fragments = FileFragmentsTransportHelper.getAllFragments(archiveFile);
		for(File fragment:fragments) {
			try {
    			File tmpFragment = new File(networkTmp.getParentFile(),fragment.getName());
    			File dstFragment = new File(networkDst.getParentFile(),fragment.getName());
    			FileCopyHelper.reliableCopy(fragment,tmpFragment);
    			tmpFragment.renameTo(dstFragment);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Разделить исходный большой файл на фрагменты (максимальный размер фрагмента указан во входном параметре)
	 * @param inputFile
	 * @param outputDirectory
	 */
	public static FileFragmentOperationResult divideIntpufileIntoFragments(File inputFile,File outputDirectory,int maxFileFragmentSizeBytes) {
		
		// пересчитываем размер фрагмента на байты 
		maxFileFragmentSizeBytes = maxFileFragmentSizeBytes * 1024 * 1024;
		log("DEBUG: maxFileFragmentSizeBytes [" + maxFileFragmentSizeBytes + "]");
		
		long lastModifiedDate = inputFile.lastModified();
		try {
			// генерируем гуид для данного файла в данную пересылку
			String fileGuid = UUID.randomUUID().toString();
			
			// считаем MD5 для всего файла
	        byte[] wholeFileMd5 = null;
	        try {
	        	wholeFileMd5=MD5Helper.getCheckSumAsBytes(inputFile.getAbsolutePath());
	        } catch (Exception e) {
	            e.printStackTrace();
	            // если не смогли посчитать MD5 всего файла это полный fail
	            return new FileFragmentOperationResult(FileFragmentOperationResultTypes.OPERATION_ERROR);
	        }
	        // переводим md5 всего файла в base64
			String wholeFileMd5Base64String = Base64.encodeBase64String(wholeFileMd5);
			
			// открываем поток входного файла
			FileInputStream fis = new FileInputStream(inputFile);

			// счиатем количество фрагментов в исходном файле
			int fragmentCount = (int)Math.round( ((double)inputFile.length()/(double)maxFileFragmentSizeBytes) + 0.5);
			log("DEBUG: fragmentCount [" + fragmentCount + "]");
			log("DEBUG: inputFile.length() [" + inputFile.length() + "]");
			
			// счетчик текущего фрагмента	
			int currentFragmentNumber = 1;

			
			int fragmentSize;
			byte[] inputFileContent = new byte[maxFileFragmentSizeBytes];
			
			while ((fragmentSize = fis.read(inputFileContent)) > 0) {
				log("DEBUG: Fragment " + currentFragmentNumber +", fragmentSize [" + fragmentSize + "]");
				
				
				Properties props = new Properties();
				// добавляем метаинформацию к текущему фрагменту
				props.setProperty(InflatedFileInfo.METADATA_PROPERTY_FRAGMENT_COUNT, "" + fragmentCount);
				props.setProperty(InflatedFileInfo.METADATA_PROPERTY_CURRENT_FRAGMENT_NUMBER, "" + currentFragmentNumber);
				props.setProperty(InflatedFileInfo.METADATA_PROPERTY_FILE_GUID, "" + fileGuid);
				props.setProperty(InflatedFileInfo.METADATA_PROPERTY_WHOLE_FILE_MF5, wholeFileMd5Base64String);
				props.setProperty(InflatedFileInfo.METADATA_PROPERTY_FILE_GEN_TIMESTAMP,"" + new Date().getTime());
				
				// создаем файл-архив для текущего фрагмента
				
				deflateWithMetainfo(
						lastModifiedDate, 
						inputFile.getName(), 
						Arrays.copyOf(inputFileContent, fragmentSize),
						new File(outputDirectory,getFragmentFileName(inputFile.getName(),currentFragmentNumber) ), props,true);
				
				// увеличиваем счетчик текущего фрагмента
				log("DEBUG: created fragment " + currentFragmentNumber);
				currentFragmentNumber++;
				
			}
			log("File " + inputFile.getName() + " was successfully divided into " +  fragmentCount + " fragments. Whole file md5 is [" + wholeFileMd5Base64String + "]");
			// закрываем поток
			fis.close();
            return new FileFragmentOperationResult(FileFragmentOperationResultTypes.FILE_SUCCESFULLY_DIVIDED_INTO_FRAGMENTS);
		} catch (Exception ex) {
			ex.printStackTrace();
			log("DEBUG: Failed devide SQLITE file into fragments, cause " + ex.getMessage());
            return new FileFragmentOperationResult(FileFragmentOperationResultTypes.OPERATION_ERROR);
		}
	}
	
	
	/**
	 * Собрать файл по фрагментам-архивам в целый разархивированный файл
	 * @param inputFolder
	 * @param fileName
	 * @param outputFile
	 */
	public static FileFragmentOperationResult collectAllFragmentsAndCreateSourceFile(File inputFile,File tempFolder) throws IOException {
		 /* алгоритм работы следующий
		  * Каждый фрагмент содержит в себе номер фрагмента и гуид сгенерированный при разбиении всего файла(он один у всех фрагментов)
		  * Структура папок. filename/fileguid/filename,filename/fileguid/metainfo
		  * 1. Создаем папку fileGuid если она не существует во времемнной папке
		  * 2. Создаем файл с именем файла(если он не сущесвует) и пишем в него в нужное место очередной фрагмент. Перед добавлением фрагмента проверяем его md5
		  * 3. Рядом с файлом храним метаинфу по которой можно определить какие фрагменты уже были загружены.
		  * 4. Когда в файл добавлены все фрагменты, проверяется его полный md5, если файл восстановлен корректно, то переносится следующему сервису
		  * 5. Если пришел фрагмент с filename но с несоответствующим fileGUID определяется его дата и сравнивается с датой сущесвующего файла и в случае если фрагмент более новый, то старая ветка GUID удаляется
		  */
		boolean fileFragmentSuccesfullyAdded = false;
		try {
			InflatedFileInfo fileInfo = getInflatedFile(inputFile,tempFolder);
			if (fileInfo == null)
				return new FileFragmentOperationResult(FileFragmentOperationResultTypes.OPERATION_ERROR);
			
			// шаг 1 создаем нужные папки
			File sTempFolder = new File(tempFolder.getAbsolutePath() + File.separator + "temp");
			File sTempFileNameFolder = new File(sTempFolder.getAbsolutePath() + File.separator + fileInfo.getFileName()+"_fragments");
			File sTempFile = new File(sTempFileNameFolder.getAbsolutePath() + File.separator + fileInfo.getAdditionalProperties().getProperty("currentFragmentNumber"));
			FileHelper.createMissingFolders(sTempFolder.getAbsolutePath(),sTempFileNameFolder.getAbsolutePath());
			
			// шаг 2 - распаковываем архив фрагмента и выбираем всю неоьходимую информацию 
			FileFragmentOperationResultTypes resultType =  updateTempFolderByCompareNewFileFragmentMetadataWithOldCollected(sTempFileNameFolder,fileInfo,false);
			if (resultType != null && resultType.equals(FileFragmentOperationResultTypes.OPERATION_IGNORED))
				return new FileFragmentOperationResult(FileFragmentOperationResultTypes.OPERATION_IGNORED);
			
			// шаг3 - добавляем данные файла 
			FileOutputStream fos = new FileOutputStream(sTempFile);
			FileInputStream fisBinaryContent = new FileInputStream(fileInfo.getFileBinaryContentTemp());
			copyOutputStreamToInputStream(fisBinaryContent, fos);
			fisBinaryContent.close();

			fos.close();
			log("File fragment successfully added to temp store.");
			
			// удаляем из памяти лишние данные
			fileInfo.clearBinaryContent();
			
			fileFragmentSuccesfullyAdded = true;
			
			// считаем колическто фрагментов
			int sourceFragmentCount = Integer.valueOf(fileInfo.getAdditionalProperties().getProperty("fragmentCount"));

			// шаг 4- если для сборки файла добавлены все фрагменты - то пытаемся склеить весь файл 
			if (checkFragmentCount(sTempFileNameFolder,sourceFragmentCount,false)) {
				log("Found all file fragments. Try to collect the whole file.");
				// склеиваем все фрагменты в один файл
				File wholeTargetFile = new File(sTempFolder.getAbsolutePath() + File.separator + fileInfo.getFileName());
				FileOutputStream fos2 = new FileOutputStream(wholeTargetFile);
				for(int i = 1;i <= sourceFragmentCount;i++) {
					FileInputStream fis = new FileInputStream(new File(sTempFileNameFolder.getAbsolutePath() + File.separator + i));
					copyOutputStreamToInputStream(fis, fos2);
					fis.close();
				}
				fos2.close();
				
				// считаем MD5 для всего файла
		        byte[] wholeFileMd5 = null;
		        try {
		        	wholeFileMd5=MD5Helper.getCheckSumAsBytes(wholeTargetFile.getAbsolutePath());
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		        // устанавливаем дату создания файла
		        wholeTargetFile.setLastModified(new SimpleDateFormat(InflatedFileInfo.METADATA_PROPERTY_DATE_PATTERN).parse(fileInfo.getFileCreateDate()).getTime());
		        
		        // удаляем папку с фрагментами. Она нам больше не нужна
	        	FileCopyHelper.reliableDeleteFolderAndSubFolders(sTempFileNameFolder);
	        	
	        	if (!Arrays.equals(wholeFileMd5, Base64.decodeBase64(fileInfo.getAdditionalProperties().getProperty("wholeFileMd5")))) {
	        		FileCopyHelper.reliableDelete(wholeTargetFile);
					log("Collected file mf5 is wrong. File was deleted.");
	        		throw new IOException("Resulted md5 file is corrupted");
	        	} else {
					log("Whole file [" + wholeTargetFile.getName() + "] was successfully collected and md5 checked.");
	        		FileCopyHelper.moveIfDestinationDoesNotExist(wholeTargetFile, new File(tempFolder,wholeTargetFile.getName()));
	        		// в данном случае возвращаем результат и имя результирующего файла 
					return new FileFragmentOperationResult(FileFragmentOperationResultTypes.WHOLE_FILE_SUCCESSFULLY_COLLECTED,wholeTargetFile.getName());
	        	}
	        		
			}
		} 
		catch (IOException ex1) {
			throw ex1;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if (fileFragmentSuccesfullyAdded)
			return new FileFragmentOperationResult(FileFragmentOperationResultTypes.FRAGMENT_SUCCESSFULLY_ADDED);
		else
			return new FileFragmentOperationResult(FileFragmentOperationResultTypes.OPERATION_RESULT_UNKNOWN);
	}
	
	/**
	 * 1. Проверяется, что файл успешно собрался
	 * 2. Если да, то возврвщает ссылка на собранный файл 
	 * 3. Если нет, то возвращается null
	 * @param fragmentFile
	 * @return
	 */
	public static File getResultFileAndCheckIfFileIsCollected(File fragmentFile) {
		String sourceFileName = getSourceFileNameFromFragmentFile(fragmentFile.getName());
		File sTempFolder = new File(fragmentFile.getParent(),"temp");
		File sTempFileNameFolder = new File(sTempFolder.getAbsolutePath() + File.separator + sourceFileName+"_fragments");
		File resultFile = new File(fragmentFile.getParent(),sourceFileName);
		
		// Если временной папки для сборки файла уже не существует, а в результирующей папке файл появился, то считаем что он успешно собрался
		if (!sTempFileNameFolder.exists() &&resultFile.exists())
			return resultFile;
		else return null;
	}
	
	/**
	 * Возвращает название исходного файла по названию архива фрагмента
	 * Если на вход подан не фрагмент- возвращается название файла без изменения.
	 * @param fragmentFileName
	 * @return
	 */
	public static String getSourceFileNameFromFragmentFile(String fragmentFileName) {
		if (!fragmentFileName.endsWith(FRAGMENT_ARCHIVE_FILE_EXTENSION))
			return fragmentFileName;
		else
			return fragmentFileName.replaceAll("\\_[\\d]*\\." + FRAGMENT_ARCHIVE_FILE_EXTENSION, "");
	}

	/* ********************* Внутренние методы ****************** */
	
	/**
	 * Добавить сообщение в тег-лог
	 * @param message
	 */
	private static void log(String message) {
		tagLogger.log(new String[]{"fileFragments"}, message);
	}
	
	/**
	 * Расчитать имя файла фрагмента
	 * @param fileName
	 * @param fragmentNumber
	 * @return
	 */
	private static String getFragmentFileName(String fileName, int fragmentNumber) {
		return fileName + "_" + fragmentNumber + "." + FRAGMENT_ARCHIVE_FILE_EXTENSION;
	}

	/**
	 * ДОбавить/удалить файловую блокировку
	 * @param filename
	 * @param result
	 */
	private static void updateFileLock(File inputFile,File archiveFolder, boolean result) {
		File f = new File(archiveFolder,getSourceFileNameFromFragmentFile(inputFile.getName()) + FILECOPY_LOK_EXT);
		if (result)
			try {
				f.createNewFile();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		else
			FileCopyHelper.reliableDelete(f); 
	}
	
	/**
	 * Проверить совпадает ли число загруженных фрагментов с общим числом фрагментов
	 * @param tempFolder
	 * @param sourceFragmentCount
	 * @param isFullFragmentName - 
	 *  true - считаем что файлы фрагментов в папке вида filename_1.fzip,filename_2.fzip,filename_3.fzip...
	 *  false - считаем что файлы фрагментов в папке вида 1,2,3,...
	 * @return
	 */
	private static boolean checkFragmentCount(File tempFolder,int sourceFragmentCount,boolean isFullFragmentName) {
		for(int i=1;i <= sourceFragmentCount;i++) {
			File tempFile = null;
			if (isFullFragmentName)
				tempFile = new File(tempFolder,getFragmentFileName(tempFolder.getName(), i));
			else
				tempFile = new File(tempFolder,"" + i);
			
			if (!tempFile.exists())
				return false;
		}
		return true;
	}
	
	/**
	 * Пробежаться по всем черновикам и удалить устаревшие
	 * @param archiveFileFolder
	 */
	private static void clearOldArchiveFiles(File archiveFileFolder) {
		File[] files = archiveFileFolder.listFiles();
		TreeMap<Long,File> fileListWithDate = new  TreeMap<Long, File>();
		for(File f:files) {
			if (f.isDirectory())
				fileListWithDate.put(f.lastModified(), f);
		}
		NavigableSet<Long> set = fileListWithDate.navigableKeySet();
		Iterator<Long> it = set.iterator();
		for(int i=0;i<fileListWithDate.size() - MAX_COUNT_ARHIVE_FILES - 1;i++) {
			Long key = it.next();
			FileCopyHelper.reliableDelete(fileListWithDate.get(key));
		}
	}
	
	/**
	 * Получить полные метаданне файла из входного архива fzip на диске
	 * @param inputFile
	 * @return
	 */
	private static InflatedFileInfo getInflatedFile(File inputFile,File tempDir) {
		InflatedFileInfo fileInfo = null;
		// получаем информацию из архива
		try {
			fileInfo = inflateWithMetainfo(inputFile,tempDir);
			log("File fragment " + inputFile.getName() + " was successfully unzipped and metadata has been read.");
			return fileInfo;
		} catch (Exception ex) {
			// Если произошла ошибка при распаковке файла и чтении метаданных - считаем, что фрагмент поврежден и игнорируем его
			FileCopyHelper.reliableDelete(inputFile);
			// выводи стектрейс ошибки
			ex.printStackTrace();
			// выходим из метода
			log("File fragment " + inputFile.getName() + " was removed because it is corrupted.");
			return null;
		}
	}
	
	private static FileFragmentOperationResultTypes updateTempFolderByCompareNewFileFragmentMetadataWithOldCollected(File tempFilenameFolder,InflatedFileInfo fileInfo,boolean needLock) throws FileNotFoundException,IOException {
		// пробуем найти файл metadata
		File metadataFile = new File(tempFilenameFolder,ZIP_METADATA_FILE_NAME);

		if (!metadataFile.exists()) {
			// если файл с метаданными не найден, то в данной папке некорректные данные, поэтому удаляем все содержимое этой папки и двигаемся дальше
			FileCopyHelper.reliableDeleteFolderContent(tempFilenameFolder);
		} else {
			// читаем метаданные сохраненных в кеше
			FileInputStream fis = new FileInputStream(metadataFile);
			Properties cacheFileMetadata = new Properties();
			cacheFileMetadata.load(fis);
			fis.close();
			// получаем значения необходимых параметров
			String oldGuidFoldername = cacheFileMetadata.getProperty("fileGuid");
			long cacheFileGenDate = Long.valueOf(cacheFileMetadata.getProperty("fileGenTimestamp"));
			long newFileGenDate = Long.valueOf(fileInfo.getAdditionalProperties().getProperty("fileGenTimestamp"));
			
			//  если новый фрагмент пришел с тем же гуидом файла, то ничего не делаем. Все в порядке 
			if (oldGuidFoldername.equals(fileInfo.getAdditionalProperties().getProperty("fileGuid"))) {
				log("File fragment matches current collected group.");
			} else {
				// пришел фрагмент файла, котоырй был сгенерирован позже того, который сейчас собирается в кеше 
				if (newFileGenDate > cacheFileGenDate) {
					// удаляем содержимое всей папки
					FileCopyHelper.reliableDeleteFolderContent(tempFilenameFolder);
					log("File fragment from new group was found. New Guid is [" + fileInfo.getAdditionalProperties().getProperty("fileGuid") + "]. Old collected file fragments was removed." );
				} else {
					log("File fragment from old group was found. File will be ignored.");
					return FileFragmentOperationResultTypes.OPERATION_IGNORED;
				}
			}
		}
		
		// Если файл с метаданными удален на предыдущем шаге или не существовал, то создаем новый файл с метаданными
		if (!metadataFile.exists()) {
			// добавляем данные метаданных
			FileOutputStream fos = null;
			PrintStream stream = null;
			try {
				fos = new FileOutputStream(metadataFile);
				stream = new PrintStream(fos);
				fileInfo.getAdditionalProperties().list(stream);
				// начинается загрузка нового файла - делаем блокировку на копирование
				if (needLock)
					updateFileLock(tempFilenameFolder, tempFilenameFolder.getParentFile(), true);
				log("Current group metadata successfully added to temp store.");
			} finally {
				try { 
					stream.close();
					fos.close();
				} catch (Exception ex) {}
			}
		}
				
		return FileFragmentOperationResultTypes.OPERATION_OK;
	}
	
	/**
	 * Скопировать бинарные данные из входного потока в выходной
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	private static void copyOutputStreamToInputStream(InputStream is, OutputStream os) throws IOException {
		byte[] a = new byte[MAX_WRITE_BUFFER_SIZE];
		int len = is.read(a);
		while (len > 0) {
			os.write(a,0,len);
			len = is.read(a);
		}		
	}
	
	/**
	 * Заархивировать файл и включить в архив метаинформацию
	 * @param lastModifiedDate
	 * @param fileName
	 * @param inputFileContent
	 * @param outputFile
	 * @param additionalProperties
	 */
	public static void deflateWithMetainfo(long lastModifiedDate,String fileName,byte[] inputFileContent,File outputFile,Properties additionalProperties,boolean needCompression) {
		ZipOutputStream zipOutputStream = null;
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			zipOutputStream = new ZipOutputStream(fos);
			if(!needCompression)
				zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
			
			// добавляем сам файл
			ZipEntry entry = new ZipEntry(fileName);
			zipOutputStream.putNextEntry(entry);
			zipOutputStream.write(inputFileContent);
			
			// формируем метаинформацию по файлу
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Properties pr = new Properties();
			
			// считаем md5
	        byte[] md5 = null;
	        try {
	            md5=MD5Helper.getCheckSumAsByteArray(inputFileContent);
				log("DEBUG: successfully calculated md5!");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        // добавляем md5
	        pr.setProperty(InflatedFileInfo.METADATA_PROPERTY_MD5_FIELD_NAME, new Base64().encodeToString(md5));
	        // добавляем дату создаяни файла
	        pr.setProperty(InflatedFileInfo.METADATA_PROPERTY_FILE_CREATE_DATE_FIELD_NAME, new SimpleDateFormat(InflatedFileInfo.METADATA_PROPERTY_DATE_PATTERN).format(lastModifiedDate));
	        // добавляем имя файла 
	        pr.setProperty(InflatedFileInfo.METADATA_PROPERTY_FILENAME_FIELD_NAME, fileName);
	        // добавляем в метаданные все дополнительные свойства, переданные извне
	        if (additionalProperties != null)
	        	pr.putAll(additionalProperties);
	        
	        // кладем файл с пропертями в архив
			PrintStream stream = new PrintStream(bos);
			pr.list(stream);
			entry = new ZipEntry("metainfo");
			bos.close();
			stream.close();
			zipOutputStream.putNextEntry(entry);
			zipOutputStream.write(bos.toByteArray());
			log("DEBUG: successfully saved file " + outputFile.getAbsolutePath());
		} catch (Exception ex) {
			ex.printStackTrace();
			log("DEBUG: Failed saved file " + outputFile.getAbsolutePath() + ", cause " + ex.getMessage());
		} finally {
			try {
				zipOutputStream.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				log("DEBUG: Failed close archive " + outputFile.getAbsolutePath() + ", cause " + ex.getMessage());
			}
		}
	}
	
	
	/**
	 * Разархивировать файл и на основе метаданных определить валидность файла 
	 * @param inputFile
	 * @param outputFile
	 * @throws IOException
	 */
	public  static InflatedFileInfo inflateWithMetainfo(File inputFile,File tempDir) throws IOException {
		
		
		FileInputStream fis = null;
		ZipInputStream zipInputStream = null;
		
		try {
			InflatedFileInfo fileInfo = new InflatedFileInfo();
			fis = new FileInputStream(inputFile);
			zipInputStream = new ZipInputStream(fis);
			Map<String,File> files = new HashMap<String,File>(); 
			
			ZipEntry entry = null;

			// Читаем все файлы в архиве и заносим их бинарные данные в карту 
			while((entry = zipInputStream.getNextEntry()) != null) {
				File tempBinaryContent = new File(tempDir,UUID.randomUUID().toString());
				FileOutputStream fos = new FileOutputStream(tempBinaryContent);
				copyOutputStreamToInputStream(zipInputStream, fos);
				fos.close();

				// кладем прочитанный файл в мапу
				files.put(entry.getName(), tempBinaryContent);
			}
			
			// читаем файл метаданных внутри архива
			Properties pr = new Properties();
			FileInputStream fisProps = new FileInputStream(files.get("metainfo"));
			pr.load(fisProps);
			fisProps.close();
			
			// удаляем файл после чтения
			FileCopyHelper.reliableDelete(files.get("metainfo"));
			
			// считаем md5
	        byte[] md5 = null;
	        byte[] controlMd5 = null;
	        try {
	            md5=MD5Helper.getCheckSumAsBytes(files.get(pr.getProperty(InflatedFileInfo.METADATA_PROPERTY_FILENAME_FIELD_NAME)).getAbsolutePath()); //  AsByteArray(files.get(pr.getProperty(METADATA_FILENAME_FIELD_NAME)));
	            controlMd5 = Base64.decodeBase64(pr.getProperty(InflatedFileInfo.METADATA_PROPERTY_MD5_FIELD_NAME)); 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
			
			if (!Arrays.equals(md5,controlMd5)) {
				// TODO: сделать нормальную обработку этой ошибки
				log("File fragment md5 is wrong!");
				throw new IOException("File md5 incorrect");
			}
			
			fileInfo.setFileCreateDate(pr.getProperty(InflatedFileInfo.METADATA_PROPERTY_FILE_CREATE_DATE_FIELD_NAME));
			fileInfo.setAdditionalProperties(pr);
			fileInfo.setFileName(pr.getProperty(InflatedFileInfo.METADATA_PROPERTY_FILENAME_FIELD_NAME));
			fileInfo.setMd5(pr.getProperty(InflatedFileInfo.METADATA_PROPERTY_MD5_FIELD_NAME));
			fileInfo.setFileBinaryContentTemp(files.get(pr.getProperty(InflatedFileInfo.METADATA_PROPERTY_FILENAME_FIELD_NAME)));

	        
			return fileInfo;
		} 
		catch (IOException ex1) {
			throw ex1;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			zipInputStream.close();
			fis.close();
		}
		
		return null;
	}
}
