package ru.sberbank.syncserver2.gui.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import ru.sberbank.syncserver2.gui.util.JSPHelper;
import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;
import ru.sberbank.syncserver2.gui.web.ShowTableController.SearchResult;
import ru.sberbank.syncserver2.gui.data.AuditRecord;
import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.db.dao.GeneratorDao;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.generator.single.OneCallablePerTagThreadPool;
import ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService;
import ru.sberbank.syncserver2.service.generator.single.data.ActionInfo;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.service.log.CSVImpl;
import ru.sberbank.syncserver2.service.log.DbLogService;
import ru.sberbank.syncserver2.service.log.GeneratorLogFile;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.log.LogMsg;
import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 06.04.14.
 */
public class GeneratorController extends ShowTableController {
    private String generatorServiceBeanCode = "singleGenerator";
    
    protected TagLogger tagLogger;
    private SingleGeneratorService generatorService;
  
	@Autowired
	private GeneratorDao generatorDao;
    
    public GeneratorController() {
        super(ShowHtmlController.class);
        tagLogger = TagLogger.getTagLogger(getClass(),LOGGER);
        numberOfColumns=3;
    }

    public String getGeneratorServiceBeanCode() {
        return generatorServiceBeanCode;
    }

    public void setGeneratorServiceBeanCode(String generatorServiceBeanCode) {
        this.generatorServiceBeanCode = generatorServiceBeanCode;
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("tasks.generator.gui") ){
            return showTasks(request, response);
        } else if(servletPath.contains("start.generator.gui") ){
            return startGeneration(request, response,false);
        } else if(servletPath.contains("stop.generator.gui") ){
            return stopGeneration(request, response);
        } else if(servletPath.contains("copyagain.generator.gui") ){
            return copyLastGeneratedAgain(request, response);
        } else if(servletPath.contains("logs.generator.gui") ){
            return showLogs(request, response);
        } else if(servletPath.contains("changeautogen.generator.gui") ){
        	return changeAutoGen(request, response);
        } else if(servletPath.contains("changegenmode.generator.gui") ){
        	return changeGenMode(request, response);
        } else if(servletPath.contains("publishdraft.generator.gui") ){
            return publishDraft(request, response);
        } else if(servletPath.contains("deletedraft.generator.gui") ){
            return deleteDraft(request, response);
        } else if(servletPath.contains("forsepublish.generator.gui") ){
            return startGeneration(request, response,true);
        } else {
            return showText("Invalid servetPath : "+servletPath);
        }
    }

    /**
     * Подготовить мапы с информацией о текущем статусе генерации заданий
     *   
     * @param actionInfos
     * @return
     */
    private List<ActionInfo> updateActionInfoForPhaseStates(List<ActionInfo> actionInfos) {
    	ExecutionTimeProfiler.start("GeneratorController.updateActionInfoForPhaseStates");
    	
    	// dataFilename:phaseCode:ActionState
    	Map<String,Map<String,ActionState>> statesGen = new HashMap<String,Map<String,ActionState>>();
    	// dataFilename:sigmahost:ActionState
        Map<String,Map<String,ActionState>> statesDeliv = new HashMap<String, Map<String,ActionState>>();
        
        // получаем общий список всех состояний по всем заданиям
        List<ActionState> actionStates = generatorDao.getCurrentStaticFilesState();
    	
        // Начинаем разбирать общий список состояний и укладывать их в нужные ячейки
        for(ActionState st: actionStates) {
        	// Если в текущем состоянии не указан файл, то это какой то мусор в БД(пропускаем его)
        	if (st.getFileName() == null || "".equals(st.getFileName()))
        		continue;
        	// Если не задан сигмахост - то это Альфа-состояние
        	if (st.getSigmaHost() == null || "".equals(st.getSigmaHost())) {
        		if (!statesGen.containsKey(st.getFileName()))
        			statesGen.put(st.getFileName(),new HashMap<String, ActionState>());
        		statesGen.get(st.getFileName()).put(st.getPhaseCode(),st);
        	}
        	// Если задан Альфа-хост то это Сигма-состояние 
        	else {
        		if (!statesDeliv.containsKey(st.getFileName()))
        			statesDeliv.put(st.getFileName(),new HashMap<String,ActionState>());
        		statesDeliv.get(st.getFileName()).put(st.getSigmaHost(), st);
        	}
        }
        
        // Добавляем в каждый ActionTask-объект информацию о статуса каждого этапа 
        for(ActionInfo info:actionInfos) {
            info.setActionGenStates(statesGen.get(info.getDataFileName()));
            info.setActionLoadStates(statesDeliv.get(info.getDataFileName()));
        }
       
    	ExecutionTimeProfiler.finish("GeneratorController.updateActionInfoForPhaseStates");
    	
        return actionInfos;
    }
    
    private ModelAndView showTasks(HttpServletRequest request, HttpServletResponse response) {
    	if ("true".equals(request.getParameter("json")))
    		return getJsonTaskInfos(request, response);
    	
        //1. Collecting data
        SingleGeneratorService service = getGeneratorServiceInstance();
        System.out.println("Start service.listActionInfo()");
        List<ActionInfo> actionInfos = updateActionInfoForPhaseStates(service.listActionInfo());
        System.out.println("Finish service.listActionInfo()");

        //2. Composing activeNodeMessage
        String activeNodeMessage = getHostMessage(request);

        //2. Composing HTML
        ModelAndView mv = new ModelAndView("listGeneratorTasks");
        mv.addObject("actionInfos",actionInfos);
        mv.addObject("activeNodeMessage",activeNodeMessage);
        return mv;
    }

    private ModelAndView startGeneration(HttpServletRequest request, HttpServletResponse response,boolean isForcePublish) {
        //1. Check if node is active
        if(!isActiveClusterNode()){
            String message = getHostMessage(request);
            ModelAndView mv = new ModelAndView("message");
            mv.addObject("message",message);
            return mv;
        }

        //2. Check data file name
        String dataFileName = request.getParameter("dataFileName");
        if(dataFileName==null){
            return showText("Undefined dataFileName");
        }
        SingleGeneratorService service = getGeneratorServiceInstance();

        //3. Dropping old logs
        DbLogService dbLogService = service.getDbLogger();
      /*  synchronized (this) {
        	String sql = "DELETE FROM SYNC_LOGS WHERE EVENT_INFO='"+dataFileName+"'";
        	dbLogService.executeSQL(sql);
		}
       */
        //4. Logging queuing
        int id = AuditHelper.write(request, "Запуск генерации (ручная)", dataFileName, AuditHelper.GENERATION);
    	if (id>-1)
    		dbLogService.logObjectEvent(LogEventType.OTHER, dataFileName, "Adding audit record with id "+id);
        	
        dbLogService.logObjectEvent(LogEventType.GEN_QUEUED, dataFileName, "Adding task for generation of " + dataFileName + " in queue");
        service.manualRun(dataFileName,isForcePublish);
        
        return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX + "tasks.generator.gui#DETAILS");
        //return showTasks(request, response);
    }

    private ModelAndView stopGeneration(HttpServletRequest request, HttpServletResponse response) {
        //1. Parsing dataFileName
        String dataFileName = request.getParameter("dataFileName");
        if(dataFileName==null){
            return showText("Undefined dataFileName");
        }

        //2. Stopping
        SingleGeneratorService service = getGeneratorServiceInstance();
        service.stopGeneration(dataFileName);

        //3. Logging cancel
        DbLogService dbLogService = service.getDbLogger();
        int runStatus = service.getRunStatus(dataFileName);
        if(runStatus== OneCallablePerTagThreadPool.RUN_STATUS.NONE){
            dbLogService.logObjectEvent(LogEventType.GEN_CANCELLED, dataFileName, "Cancelled generation of " + dataFileName+" (immediate cancel)");
        } else {
            dbLogService.logObjectEvent(LogEventType.GEN_CANCELLED, dataFileName, "Scheduled cancelling generation of " + dataFileName);
        }
        AuditHelper.write(request, "Отмена генерации файла", dataFileName, AuditHelper.GENERATION);
        return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX + "tasks.generator.gui");        
    }

    private ModelAndView copyLastGeneratedAgain(HttpServletRequest request, HttpServletResponse response) {
        String dataFileName = request.getParameter("dataFileName");
        if(dataFileName==null){
            return showText("Undefined dataFileName");
        }
        SingleGeneratorService service = getGeneratorServiceInstance();
        service.copyLastGeneratedAgain(dataFileName);
        
        AuditHelper.write(request, "Повторная отправка сгенерированного файла", dataFileName, AuditHelper.GENERATION);
        return showTasks(request,response);
    }
    
    /**
     * Возвращает JSON данные по статусу задач
     * @param request
     * @param response
     * @return
     */
    private ModelAndView getJsonTaskInfos(HttpServletRequest request, HttpServletResponse response) {
        SingleGeneratorService service = getGeneratorServiceInstance();
        System.out.println("Start service.listActionInfo()");
        List<ActionInfo> actionInfos = updateActionInfoForPhaseStates(service.listActionInfo());
        System.out.println("Finish service.listActionInfo()");
        ModelAndView mv = new ModelAndView("listGeneratorTasksInfoJson");
        mv.addObject("actionInfos",actionInfos);
        return mv;
    }
    
    private ModelAndView showLogs(HttpServletRequest request, HttpServletResponse response) {
    	String dataFileName = request.getParameter("dataFileName");
        if(dataFileName==null){
            return showText("Undefined dataFileName");
        }
		if ("true".equals(request.getParameter("table"))) {
			transmit(request, response);
	        return null;
		}
        boolean debug = "true".equalsIgnoreCase(request.getParameter("debug"));
		SingleGeneratorService single = getGeneratorServiceInstance();
        List<String> listFiles = single.getDataFileNames();
        if (!listFiles.contains(dataFileName))
        	listFiles.add(dataFileName);
                
        List<String> listFormatedDates = new ArrayList<String>();
        List<Long> listDates = single.getDbLogger().getGenerationDates(dataFileName);        
        for (Long timestamp: listDates) {
        	listFormatedDates.add(JSPFormatPool.formatDateAndTime2(timestamp));
        }
        
        String date = request.getParameter("date");
        if (date==null || date.equals("")) {
        	date = listFormatedDates.size()>0?listFormatedDates.get(0):"";
        }

        ModelAndView mv = new ModelAndView("listGeneratorLogs");
        mv.addObject("dataFileName", dataFileName);
        mv.addObject("allowedFiles", listFiles);
        mv.addObject("generationDates", listFormatedDates);
        mv.addObject("date", date);
        mv.addObject("debug",debug);
        return mv;
    }
        
    private SingleGeneratorService getGeneratorServiceInstance(){
    	if (generatorService == null) {
    		ServiceManager serviceManager = ServiceManager.getInstance();
            ServiceContainer serviceContainer = serviceManager.findServiceByBeanCode(generatorServiceBeanCode);
            generatorService = (SingleGeneratorService) serviceContainer.getService();
    	}        
        return generatorService;
    }

    private boolean isActiveClusterNode(){
        ClusterManager clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
        boolean active = clusterManager==null || clusterManager.isActive();
        return active;
    }

    private String getActiveHost(){
        ClusterManager clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
        String activeHostName = clusterManager==null ? null:clusterManager.getActiveHostName();
        return activeHostName;

    }

    private String getHostMessage(HttpServletRequest request){
        boolean isActive = isActiveClusterNode();
        String activeHostMessage = getActiveHost();
        if(isActive){
            return "<div style=\"color:#0A6B31\"/>Это активный узел кластера</div>";
        } else {
            String link = HttpRequestUtils.getServerAddressWithOtherServer(request, activeHostMessage) +"/generator/gui/tasks.generator.gui";
            String href = "<a href=\""+link+"\">"+"Активный узел</a>";
            return "<div style=\"color:red\"/>Это пассивный узел кластера. Пожалуйста используйте "+href + "</div>";
        }
    }
    
    /**
     * Получить текущзий контекст безопасности
     * @param request
     * @return
     */
    public static AuthContext getAuthContext(HttpServletRequest request) {
        HttpSession session = request.getSession();
        AuthContext ctx = (AuthContext) session.getAttribute("user");
        return ctx;
    }
    
	@Override
	protected SearchResult search(HttpServletRequest request, int orderCol,
			int direction, List<String> searchValues, int startIndex,
			int numberOfMessages) {
		String dataFileName = request.getParameter("dataFileName");
		String startDate = request.getParameter("date");
		boolean debug = "true".equalsIgnoreCase(request.getParameter("debug"));
   
		Date date = JSPFormatPool.parseDate(startDate);
		Object[] logs = generatorDao.getLogsFor(dataFileName, date, debug, startIndex, numberOfMessages);
		if (logs.length>1)
			return new SearchResult((List<GenerationLogMsg>)logs[0], (Integer)logs[1], (Integer)logs[1]);
		else 
			return new SearchResult((List<GenerationLogMsg>)logs[0], -1, -1);
	}

	@Override
	protected void generateFile(HttpServletRequest request,
			HttpServletResponse response, SearchResult searchResult) {
		GeneratorLogFile logFile = new CSVImpl();
		logFile.generateFile(response, (List<GenerationLogMsg>)searchResult.getResults());		
	}
    
	public static class GenerationLogMsg implements Iterable<String>{
		private Date date;
		private String text;
		private LogEventType eventType;
		
		public GenerationLogMsg(Date eventTime, LogEventType eventType,
				String eventDesc) {
			super();
			this.date = eventTime;
			this.eventType = eventType;
			this.text = eventDesc;
		}
		
		public GenerationLogMsg(ResultSet rs) throws SQLException {
	        this.date = rs.getTimestamp("EVENT_TIME");
	        this.eventType = LogEventType.valueOf(rs.getString("EVENT_TYPE"));
	        this.text = rs.getString("EVENT_DESC");
		}

		public Date getDate() {
			return date;			
		}

		public LogEventType getEventType() {
			return eventType;
		}

		public String getText() {
			return text;
		}

		@Override
		public Iterator<String> iterator() {
			return new Iterator<String>() {
				int i = 0;
				@Override
				public boolean hasNext() {
					return i<3;
				}

				@Override
				public String next() {
					i++;
					switch (i) {
						case 1: return JSPFormatPool.formatDateAndTime2(date);
						case 2: return eventType.name();
						case 3: return text;
					}
					return null;
				}

				@Override
				public void remove() {					
				}				
			};
		}
		
	}
	
    /**
     * Метод обработки запроса на включение/отключение автогенерации 
     * @param request
     * @param response
     * @return
     */
    private ModelAndView changeAutoGen(HttpServletRequest request, HttpServletResponse response) {
    	
    	// Получаем входные параметры queryString
    	String appCode = request.getParameter("appCode");
    	String dataFileName = request.getParameter("fileName");
    	boolean enabled = "true".equals(request.getParameter("enabled"));
    	
    	// меняем в БД флаг автоматической генерации задачи
    	// Задача должна присутствовать в SYNC_CACHED_STATIC_FILES и определяеться по appCode + fileName
    	generatorDao.changeStaticFileAutoGenStatus(appCode, dataFileName, enabled);
    	
    	tagLogger.log("TaskGenerationStatus", "User \"" + getAuthContext(request).getEmployee().getEmployeeEmail() + "\" changed " + dataFileName +"(" + appCode + ") task autogenerated status to " + (enabled?"on":"off"));
    	
        AuditHelper.write(request, (enabled?"Включение":"Отключение") + " автогенерации файла", dataFileName, AuditHelper.GENERATION);
    	// переадресуем на страницу списка задач
    	return showTasks(request, response);
    }

    /**
     * Метод обработки запроса на включение/отключение автогенерации 
     * @param request
     * @param response
     * @return
     */
    private ModelAndView changeGenMode(HttpServletRequest request, HttpServletResponse response) {
    	
    	// Получаем входные параметры queryString
    	String appCode = request.getParameter("appCode");
    	String dataFileName = request.getParameter("fileName");
    	boolean enabled = "true".equals(request.getParameter("enabled"));
    	
    	// меняем в БД флаг автоматической генерации задачи
    	// Задача должна присутствовать в SYNC_CACHED_STATIC_FILES и определяеться по appCode + fileName
    	generatorDao.changeStaticFileGenerationMode(appCode, dataFileName, enabled);
    	
    	tagLogger.log("TaskGenerationStatus", "User \"" + getAuthContext(request).getEmployee().getEmployeeEmail() + "\" changed " + dataFileName +"(" + appCode + ") task autogenerated status to " + (enabled?"on":"off"));
    	AuditHelper.write(request, "Изменение режима генерации файла","Файл "+dataFileName+" переведен в режим "+(enabled?"'Черновик'":"'Публикация'"), AuditHelper.GENERATION);
        
    	// переадресуем на страницу списка задач
    	return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX + "tasks.generator.gui");
    }

    /**
     * Опубликовать черновик
     * @param request
     * @param response
     * @return
     */
    private ModelAndView publishDraft(HttpServletRequest request, HttpServletResponse response) {
    	String appCode = request.getParameter("appCode");
    	String dataFileName = request.getParameter("dataFileName");
    	
    	generatorDao.publishCurrentStaticFileDraft(appCode,dataFileName);
    	AuditHelper.write(request, "Публикация черновика", dataFileName, AuditHelper.GENERATION);
    	return showTasks(request, response);
    }

    /**
     * Удалить черновик
     * @param request
     * @param response
     * @return
     */
    private ModelAndView deleteDraft(HttpServletRequest request, HttpServletResponse response) {
    	String appCode = request.getParameter("appCode");
    	String dataFileName = request.getParameter("dataFileName");
    	
    	generatorDao.deleteCurrentStaticFileDraft(appCode,dataFileName);
    	AuditHelper.write(request, "Удаление черновика", dataFileName, AuditHelper.GENERATION);
        
    	return showTasks(request, response);
    }

}
