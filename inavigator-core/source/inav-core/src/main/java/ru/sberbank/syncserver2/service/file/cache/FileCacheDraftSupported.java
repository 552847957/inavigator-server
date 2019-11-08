package ru.sberbank.syncserver2.service.file.cache;

import ru.sberbank.syncserver2.service.core.event.impl.FileLoadedToFileCacheEventInfo;
import ru.sberbank.syncserver2.service.file.FileServiceRequest;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileCacheDraftSupported extends FileCache {

	/**
	 * Получить главный fileloader для текущего filecache
	 * @return
	 */
	private AbstractFileLoader getMainFileLoader() {
		Iterator<AbstractFileLoader> it = getLoaders().iterator();
		if (it.hasNext())
			return it.next();
		else
			return null;
	}


	/**
	 * проверить имеет ли пользователь права на чтение черновика
	 * @param app
	 * @param email
	 * @return
	 */
	private boolean hasDraftRights(String app,String email) {

		boolean hasDraftRights = false;
		SingleFileStatusCacheService singleFileStatusCacheService = null;
		Iterator<AbstractFileLoader> it = getLoaders().iterator();
		while (it.hasNext() && singleFileStatusCacheService == null)
			singleFileStatusCacheService = it.next().getFileStatusCacheService();

		if (singleFileStatusCacheService != null)
			hasDraftRights = singleFileStatusCacheService.hasRightsForDraft(app, email);

		return hasDraftRights;
	}

	/**
	 * ключевой метод для механизма черновиков - определяет какой из закешированных файлов (черновик / чистовик) использовать
	 * @param cacheValue
	 * @param hasDraftRights
	 * @param cachedFileList
	 * @return
	 */
	private CacheValue getCacheValue(CacheValue cacheValue,boolean hasDraftRights, Map<String,CacheValue> cachedFileList) {

		// если присутсвует чистовик, то драфт пропускаем
		if (cacheValue.getFileInfo().isDraft() &&
			 cachedFileList.containsKey(cacheValue.getFileInfo().getId()))
			return null;

		// если права на драфт есть и драфт существует, то возвращает драфт
		if (hasDraftRights && cachedFileList.containsKey(cacheValue.getFileInfo().getIdForDraft())) {
	    	return cachedFileList.get(cacheValue.getFileInfo().getIdForDraft());
	    // иначе, если существует опубликованный возращаем опубликованный
	    } else if (cachedFileList.containsKey(cacheValue.getFileInfo().getId())) {
	    	return cachedFileList.get(cacheValue.getFileInfo().getId());
	    // Иначе возращаем ничего
	    } else
	    	return null;
	}

	@Override
    public FileInfoList getFileList(String app,FileServiceRequest fileServiceRequest) {
		// если не передали объект запроса, то запрос обрабатывать смысла нет * в отсутствии Email)
		if (fileServiceRequest == null)
			return super.getFileList(app);

		Map<String,CacheValue> appFiles = cache.get(fileServiceRequest.getApp());
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		boolean hasDraftRights = hasDraftRights(fileServiceRequest.getApp(), fileServiceRequest.getUserEmail());


		if (appFiles != null) {
			for (String key:appFiles.keySet()) {
			    CacheValue cacheValue = getCacheValue(
			    			appFiles.get(key)
			    			,hasDraftRights
			    			,appFiles);

			    if (cacheValue != null) {
					FileInfo fileInfo = cacheValue.getFileInfo();
					fileInfo = processSkipPreview(fileServiceRequest, fileInfo);
					fileInfos.add(fileInfo);
				}
			}
		}

		//3. Compose result
		FileInfoList result = new FileInfoList(fileInfos);
		return result;
	}


	@Override
	protected CacheValue getCacheValueFromAppMap(
			FileServiceRequest fileServiceRequest,
			Map<String, CacheValue> appFiles, String fileId, String app) {
		return getCacheValue(
				appFiles.get(fileId)
				,hasDraftRights(fileServiceRequest.getApp()
				,fileServiceRequest.getUserEmail()), appFiles);
	}

	/**
	 * Сравнить md5 черновика и чистовика и провести работы по публикации черновика в случае, если он был опубликован.
	 * @param app
	 * @param filename
	 * @param draftMd5
	 * @param publishMd5
	 */
    public void checkMd5OfFilesAndPublish(String app,String fileId, String draftMd5,String publishMd5) {
    	// если файла для данного приложения не загружены в кеш, то смысла в вызове метода нет
    	if (!cache.containsKey(app))
    		return;
    	CacheValue publicCacheValue =  cache.get(app).get(fileId);
    	CacheValue draftCacheValue = cache.get(app).get(FileInfo.generateDraftFileId(fileId));

    	// если хотя бы один из файлов отсутствует в кеше, то смысла в дальнейшем вызове метода нет
    	if (draftCacheValue == null)
    		return;

    	// Определили, что у fileCache в статусе черновик лежат данные, которые в альфе находятся в опубликованном состоянии.
    	// запускаем процесс публикации
    	if (draftCacheValue.getFileInfo().getDataMD5().equals(publishMd5)) {
    		tagLogger.log("Found publications event: [" + app + "\\" + fileId + "], localDraftCacheMd5=" + draftCacheValue.getFileInfo().getDataMD5() + ((publicCacheValue != null)?(", localPublishCacheMd5=" + publicCacheValue.getFileInfo().getDataMD5()):"") + ", AlphaDraftMd5=" + draftMd5 + ", AlphaPublishMd5=" + publishMd5);

    		// если текущая опубликованная версия совпадает с черновиком, то просто удаляем черновик(см
    		if ((publicCacheValue != null) && publicCacheValue.getFileInfo().getDataMD5().equals(draftCacheValue.getFileInfo().getDataMD5())) {
    			getMainFileLoader().removeFileFromCache(app,FileInfo.generateDraftFileId(fileId));
        		tagLogger.log("Draft md5 equals publish Md5. Removing draft from cache.");
    		} else {
    			//Lock lock = null;
    			try {
		        //        lock = (ReentrantReadWriteLock.WriteLock) cacheLock.writeLock();
		        //        lock.lock();

					synchronized(cache) {
						if (publicCacheValue != null) {
							publicCacheValue.clearMemory();

							// 1. Копируеи данные файла из черновика
							publicCacheValue.setFileInfo(draftCacheValue.getFileInfo());
							publicCacheValue.setChunks(draftCacheValue.getChunks());

							// 2. Меняем статус на черновик
							publicCacheValue.getFileInfo().setDraft(false);

							// 3. Этап обновляем данные в каталоге
							getMainFileLoader().replaceOneFileEntryWithAnother(app, fileId, FileInfo.generateDraftFileId(fileId));

							// 4. Обновляем данные на диске
							getMainFileLoader().updateFileInfoToCacheFolder(fileId, publicCacheValue.getFileInfo());

							// обнуляем чанки черновика, чтобы при вызове следующего оператора переписанные в опубликованную версию данные не были удалены из памяти
							draftCacheValue.setChunks(null);

							// удаляем данные под draft ячейкой из кеша
							removeFileFromCache(app, FileInfo.generateDraftFileId(fileId));
						} else {
							// меняем статус записи
							draftCacheValue.getFileInfo().setDraft(false);
							publicCacheValue = new CacheValue(draftCacheValue.getFileInfo(), draftCacheValue.getChunks());
							cache.get(app).put(fileId, publicCacheValue);
							getMainFileLoader().replaceOneFileEntryWithAnother(app, fileId, FileInfo.generateDraftFileId(fileId));
							getMainFileLoader().updateFileInfoToCacheFolder(fileId, publicCacheValue.getFileInfo());
							// удаляем данные под draft ячейкой из кеша
							removeFileFromCache(app, FileInfo.generateDraftFileId(fileId));
						}

						getServiceContainer().getServiceManager().getSystemEventHandler().performEvent(new FileLoadedToFileCacheEventInfo(publicCacheValue.getFileInfo()));
						tagLogger.log("Draft was published succesfully(Draft md5 not equals publish Md5). Exchange file cache finished.");
					}
    			}
    			finally {
    			//	if (lock != null)
    			//		lock.unlock();
    			}
    		}


    	}

    }

    /**
     * Проверить Md5 файла и в случае соответствия удалить черновик
     * @param app
     * @param fileId
     * @param draftMd5
     */
    public void checkMd5OfFilesAndDeleteDraft(String app,String fileId, String draftMd5) {
    	// если файла для данного приложения не загружены в кеш, то смысла в вызове метода нет
    	if (!cache.containsKey(app))
    		return;

    	CacheValue draftCacheValue = cache.get(app).get(FileInfo.generateDraftFileId(fileId));

    	// если драфт-файл отсутствует в кеше, или md5 черновика в альфе не обнулен, то выходим из метода
    	if (draftCacheValue == null || !((draftMd5 == null) || (draftMd5.equals(""))) )
    		return;
    	tagLogger.log("Found delete draft event: [" + app + "\\" + fileId + "], localDraftCacheMd5=" + draftCacheValue.getFileInfo().getDataMD5());
    	try {
	    	getMainFileLoader().removeFileFromCache(app,FileInfo.generateDraftFileId(fileId));
			tagLogger.log("Draft was succesfully removed from cache.");
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}

    }

}
