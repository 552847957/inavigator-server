package ru.sberbank.syncserver2.service.file.cache;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileStatusInfo;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SingleFileStatusCacheService extends SingleThreadBackgroundService {

	public SingleFileStatusCacheService() {
		super(60);
	}

	/**
	 * Имя SQL шаблона для подгрузки статуса файлов
	 */
	private String fileStatusRequestTemplateName = "SYNCSERVER.SINGLE_FILES_REQUEST_STATUS";

	/**
	 * Имя SQL шаблона для подгрузки информации о пользователях
	 */
	private String userGroupRequestTemplateName = "SYNCSERVER.GENERATOR_USER_GROUP_DRAFT";

	/**
	 * Набор групп пользователей для которых разрешен предпросмотр черновика. Разделен по различным приложениям
	 */
	private ConcurrentMap<String, Set<String>> userGroupsForDraftsByAppMap = null;

	/**
	 * Набор групп файлов со статусами
	 */
	private ConcurrentMap<String, Map<String,FileStatusInfo>> fileStatusMap = null;

	/**
	 * Ссылка на сервис для отправки данных через DP
	 */
	private DataPowerService datapowerService = null;

	/**
	 * Сервис для работы с кешем файлов
	 */
	private FileCacheDraftSupported fileCacheService = null;

	/**
	 * Код бина сервиса для single/FileCache
	 */
	private String fileCacheBeanCode = "misFileCache";

	/**
	 * код бина для online/datapowerService
	 */
	private String datapowerServiceBeanCode = "misDataPowerService";

	/**
	 * Имя service для попадания в БД генератора
	 */
	private String generatorServiceName = "mis-generator";

	@Override
	public void doInit() {

		// при каждом ините сбрасывает мапы
		userGroupsForDraftsByAppMap = new ConcurrentHashMap<String, Set<String>>();
		fileStatusMap = new ConcurrentHashMap<String, Map<String,FileStatusInfo>>();

		// создаем ссылки на необходимые для работы сервисы
		fileCacheService = (FileCacheDraftSupported)this.getServiceContainer().getServiceManager().findServiceByBeanCode(fileCacheBeanCode).getService();
		datapowerService = (DataPowerService)this.getServiceContainer().getServiceManager().findServiceByBeanCode(datapowerServiceBeanCode).getService();

	}

	/**
	 * Запустить обновление списка групп пользвоателей которым достпны драфты
	 */
	private void updateUserGroupsForDraft() {
		OnlineRequest onlineRequest = new OnlineRequest();
		onlineRequest.setStoredProcedure(userGroupRequestTemplateName);
		onlineRequest.setService(generatorServiceName);
		DataResponse response = datapowerService.request(onlineRequest);
		if (response.getResult() != null && response.getResult().equals(Result.OK)) {
			List<DatasetRow> rows = response.getDataset().getRows();

			// заполняем временную мапу обновленными данными
			Map<String,Set<String>> tempData = new HashMap<String, Set<String>>();
			if (rows != null) {
				for (DatasetRow row:rows) {
					String appId = row.getValues().get(0).toUpperCase();
					String email = row.getValues().get(1).toUpperCase();
					if (!tempData.containsKey(appId))
						tempData.put(appId,new HashSet<String>());
					tempData.get(appId).add(email);
				}
			}

			// подменяем списки в основном кеше
			for(String app:tempData.keySet()) {
				if (!userGroupsForDraftsByAppMap.containsKey(app))
					userGroupsForDraftsByAppMap.put(app,tempData.get(app));
				else
					userGroupsForDraftsByAppMap.replace(app, tempData.get(app));
			}

			for(String app:userGroupsForDraftsByAppMap.keySet()) {
				if (!tempData.containsKey(app))
					userGroupsForDraftsByAppMap.remove(app);
			}
		} else
			tagLogger.log("Cannot make request to alpha GENERATOR_USER_GROUPS. Response is " + (response != null?response.toString():"null"));
	}

	/**
	 * Запустить обновление списка статусов всех файлов
	 */
	private void updateFileStatus() {
		OnlineRequest onlineRequest = new OnlineRequest();
		onlineRequest.setStoredProcedure(fileStatusRequestTemplateName);
		onlineRequest.setService(generatorServiceName);
		DataResponse response = datapowerService.request(onlineRequest);

		if (response.getResult() != null && response.getResult().equals(Result.OK)) {
			List<DatasetRow> rows = response.getDataset().getRows();

			// заполняем временную мапу обновленными данными
			Map<String,Map<String,FileStatusInfo>> tempData = new HashMap<String, Map<String,FileStatusInfo>>();
			for (DatasetRow row:rows) {
				FileStatusInfo fileStatusInfo = new FileStatusInfo(
						row.getValues().get(1)!= null?row.getValues().get(1):"",
						row.getValues().get(2)!= null?row.getValues().get(2):"",
						row.getValues().get(3)!= null?row.getValues().get(3):"",
						row.getValues().get(4)!= null?row.getValues().get(4):"",
						row.getValues().get(5)!= null?row.getValues().get(5).equals("1"):false);

				// Определение события публикации черновика
				fileCacheService.checkMd5OfFilesAndPublish(fileStatusInfo.getAppId(), fileStatusInfo.getFileId(), fileStatusInfo.getDraftMd5(), fileStatusInfo.getPublishMd5());

				// Определение события удаления черновика
				fileCacheService.checkMd5OfFilesAndDeleteDraft(fileStatusInfo.getAppId(), fileStatusInfo.getFileId(), fileStatusInfo.getDraftMd5());

				if (!tempData.containsKey(fileStatusInfo.getAppId()))
					tempData.put(fileStatusInfo.getAppId(),new HashMap<String,FileStatusInfo>());
				tempData.get(fileStatusInfo.getAppId()).put(fileStatusInfo.getFileId(),fileStatusInfo);
			}

			// подменяем списки файлов по приложениям в основном кеше
			for(String app:tempData.keySet()) {
				if (!fileStatusMap.containsKey(app))
					fileStatusMap.put(app,tempData.get(app));
				else
					fileStatusMap.replace(app, tempData.get(app));
			}
		} else
			tagLogger.log("Cannot make request to alpha SYNC_CACHE_STATIC_FILES. Response is " + (response != null?response.toString():"null"));

	}

	@Override
	public void doRun() {
		updateUserGroupsForDraft();
		updateFileStatus();
	}

	/**
	 * По подгруженным данным проверить есть ли у пользователя права на просмотр драфтов для текущего приложения
	 * @param app
	 * @param email
	 * @return
	 */
	public boolean hasRightsForDraft(String app,String email) {
		Set<String> emails = userGroupsForDraftsByAppMap.get((app != null)?app.toUpperCase():"");
		if (emails != null)
			return emails.contains((email != null)?email.toUpperCase():"");
		else
			return false;
	}

	/**
	 * Определить для нового файла статус (черновик/чистовик)
	 * @param fileInfo
	 */
	public boolean updateFileInfoStatusByCachedValue(FileInfo fileInfo) {
		tagLogger.log("Process File " + fileInfo.getName() + ". Updating draft status... ");
		// сохраняем старое значение статуса файла
		boolean oldDraftValue = fileInfo.isDraft();
		try {
			FileStatusInfo cachedFileStatusInfo = fileStatusMap.get(fileInfo.getApp()).get(fileInfo.getId());

			// если MD5 нового файла совпадает с md5 черновика, который генерировался в альфе, то ставим файлу статус черновика
			if (fileInfo.getDataMD5().toUpperCase().equals(cachedFileStatusInfo.getDraftMd5()!= null?cachedFileStatusInfo.getDraftMd5().toUpperCase():null)) {
				fileInfo.setDraft(true);
				tagLogger.log("File md5 equals alpha draftMd5. Status of file " + fileInfo.getName() + " set to draft.");
			}
			// если MD5 нового файла совпадает с md5 чистовика, который генерировался в альфе, то ставим файлу статус чистовик
			else if (fileInfo.getDataMD5().toUpperCase().equals(cachedFileStatusInfo.getPublishMd5()!= null?cachedFileStatusInfo.getPublishMd5().toUpperCase():null)) {
				fileInfo.setDraft(false);
				tagLogger.log("File md5 equals alpha publishedMd5. Status of file " + fileInfo.getName() + " set to published.");
			}
			else {
				tagLogger.log("File md5 not equals alpha draftMd5/publishMd5. Status of file " + fileInfo.getName() + " set to draft because of current generation mode.");
				// если в альфе не указан данный файл( вероятней всего запустили повторную генерацию)
				fileInfo.setDraft(cachedFileStatusInfo.isGenerationModeDraft());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			tagLogger.log("Error while analyzing new file draft status (" + ex.getMessage() + ")");
		}
		// вернем признак - произошло ли изменение статуса
		return oldDraftValue != fileInfo.isDraft();
	}

	public String getFileStatusRequestTemplateName() {
		return fileStatusRequestTemplateName;
	}

	public void setFileStatusRequestTemplateName(
			String fileStatusRequestTemplateName) {
		this.fileStatusRequestTemplateName = fileStatusRequestTemplateName;
	}

	public String getUserGroupRequestTemplateName() {
		return userGroupRequestTemplateName;
	}

	public void setUserGroupRequestTemplateName(String userGroupRequestTemplateName) {
		this.userGroupRequestTemplateName = userGroupRequestTemplateName;
	}

	public ConcurrentMap<String, Set<String>> getUserGroupsForDraftsByAppMap() {
		return userGroupsForDraftsByAppMap;
	}

	public void setUserGroupsForDraftsByAppMap(
			ConcurrentMap<String, Set<String>> userGroupsForDraftsByAppMap) {
		this.userGroupsForDraftsByAppMap = userGroupsForDraftsByAppMap;
	}

	public ConcurrentMap<String, Map<String, FileStatusInfo>> getFileStatusMap() {
		return fileStatusMap;
	}

	public void setFileStatusMap(
			ConcurrentMap<String, Map<String, FileStatusInfo>> fileStatusMap) {
		this.fileStatusMap = fileStatusMap;
	}

	public DataPowerService getDatapowerService() {
		return datapowerService;
	}

	public void setDatapowerService(DataPowerService datapowerService) {
		this.datapowerService = datapowerService;
	}

	public FileCacheDraftSupported getFileCacheService() {
		return fileCacheService;
	}

	public void setFileCacheService(FileCacheDraftSupported fileCacheService) {
		this.fileCacheService = fileCacheService;
	}

	public String getFileCacheBeanCode() {
		return fileCacheBeanCode;
	}

	public void setFileCacheBeanCode(String fileCacheBeanCode) {
		this.fileCacheBeanCode = fileCacheBeanCode;
	}

	public String getDatapowerServiceBeanCode() {
		return datapowerServiceBeanCode;
	}

	public void setDatapowerServiceBeanCode(String datapowerServiceBeanCode) {
		this.datapowerServiceBeanCode = datapowerServiceBeanCode;
	}

	public String getGeneratorServiceName() {
		return generatorServiceName;
	}

	public void setGeneratorServiceName(String generatorServiceName) {
		this.generatorServiceName = generatorServiceName;
	}



}
