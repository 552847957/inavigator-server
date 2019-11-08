package ru.sberbank.syncserver2.service.generator.single.pub;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.XmlPublicService;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService;
import ru.sberbank.syncserver2.service.generator.single.data.ETLAction;
import ru.sberbank.syncserver2.service.generator.single.pub.GetAutoGenStatusesResponse.AutoGenStatus;

public class GeneratorAutogenStatusPublicService extends XmlPublicService {

	
	
	@Override
	protected Object xmlRequest(HttpServletRequest request,
			HttpServletResponse response, Object xmlInput) {
		// TODO Auto-generated method stub
		SimpleDateFormat _sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		if (xmlInput instanceof GetAutoGenStatusesRequest) {
			GetAutoGenStatusesResponse responseList = new GetAutoGenStatusesResponse();
			
	        ClusterManager clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
	        if(clusterManager!=null && !clusterManager.isActive()) {
	        	responseList.setActive(false);
	        	return responseList;
	        } else
	        	responseList.setActive(true);
			
			// получаем ссылку на класс сервиса для генерации файлов. Вся логика работы с автоматическим запуском приложений зашита в нем или его дочерних объектах
			SingleGeneratorService generatorService = (SingleGeneratorService)ServiceManager.getInstance().findFirstServiceByClassCode(SingleGeneratorService.class);
			
			// получаем список файлов, по которым автоматическая генерация включена в UI ( на странице заданий в генераторе)
	        Map<String,StaticFileInfo> cachedStaticFileInfo = generatorService.getConfigManager().getEnabledAutoGenStaticFileInfoSet(); 

	        // получаем полный список кодов etl-действий
			List<String> actions = generatorService.getConfigManager().listActions();
	
			responseList.setStatuses(new ArrayList<GetAutoGenStatusesResponse.AutoGenStatus>());
			
			for(String actionName:actions) {
				ETLAction action = generatorService.getConfigManager().getAction(actionName);
				
				// если автоматическая генерация для действия не активирована, то пропускаем данное действие
				if (!action.isAutoRun()) continue;

	            // если текущее действие не содержится во множетсве разрешенных действий, то пропускаем автоматический запуск
	            boolean isAutoGenerationUIDisabled = (!cachedStaticFileInfo.containsKey(action.getFullName()) || !cachedStaticFileInfo.get(action.getFullName()).isAutoGenEnabled()); 
				
				List<Timestamp> dataActualTimes = generatorService.getConfigManager().getTaskScheduler().getDataActualTimes(action);
				List<Timestamp> dataJobTimes = generatorService.getConfigManager().getTaskScheduler().getLastJobDataTimes(action);

				AutoGenStatus status = new GetAutoGenStatusesResponse.AutoGenStatus();
				// ДАлее по каждому статусу заполняем данные
				status.setFileName(action.getDataFileName());
				status.setApplicationName(action.getPatternName());
				status.setAutoGenUIDisabled(isAutoGenerationUIDisabled);

				if (dataActualTimes != null && dataJobTimes != null) {
					// создаем
					status.setMisRequestError(false);
					status.setGeneratorDbReadaleDate(new ArrayList<String>());
					status.setMisDbReadaleDates(new ArrayList<String>());
					status.setGeneratorDbTimeStamp(new ArrayList<Long>());
					status.setMisDbTimeStamps(new ArrayList<Long>());
					for(int i=0;i<dataActualTimes.size();i++) {
						
						status.getMisDbTimeStamps().add(dataActualTimes.get(i) != null ? dataActualTimes.get(i).getTime():null);
						status.getMisDbReadaleDates().add(dataActualTimes.get(i) != null ? _sdf.format(dataActualTimes.get(i).getTime()):null);
						if ((dataJobTimes.size() >= (i-1)) &&  dataJobTimes.get(i) != null) {
							status.getGeneratorDbTimeStamp().add(dataJobTimes.get(i) != null ? dataJobTimes.get(i).getTime():null);
							status.getGeneratorDbReadaleDate().add(dataJobTimes.get(i) != null ? _sdf.format(dataJobTimes.get(i).getTime()):null);
						}

					}
				} else if (dataActualTimes == null) {
					status.setMisRequestError(true);
				}
				responseList.getStatuses().add(status);
			}
			return responseList;
		}
		
		
		return null;
	}

	@Override
	protected Class[] getSupportedXmlClasses() {
		// TODO Auto-generated method stub
		return new Class[] {GetAutoGenStatusesResponse.class,GetAutoGenStatusesRequest.class};
	}



}
