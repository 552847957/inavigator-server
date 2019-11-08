package ru.sberbank.syncserver2.gui.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.sberbank.syncserver2.gui.web.GeneratorController.GenerationLogMsg;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;


@Component
public class GeneratorDao extends BaseDao {
   
	/**
     * Получить текущее состояние генерации статик файлов
     * @return
     */
    public List<ActionState> getCurrentStaticFilesState() {
    	return jdbcTemplate.query("exec SP_SYNC_STATIC_FILES_GEN_GET_CURRENT_STATE", new RowMapper<ActionState>() {

            @Override
            public ActionState mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new ActionState(rs.getString("STATUS_CODE"), rs.getString("STATUS_NAME"),rs.getString("PHASE_CODE"), rs.getString("PHASE_NAME"), rs.getString("FILE_NAME"), rs.getString("SIGMA_HOST"),((rs.getDate("GEN_HISTORY_DATE")!=null)?new Date(rs.getTimestamp("GEN_HISTORY_DATE").getTime()):null),rs.getString("WEB_HOST_NAME"));
            }

        });    
    }
    
    /**
     * Включить/Отключить автоматическую генерацию для задачи в БД 
     * @param appCode
     * @param dataFileName
     * @param enabled
     */
    public void changeStaticFileAutoGenStatus(String appCode,String dataFileName,boolean enabled) {
   		String sql = "UPDATE SYNC_CACHE_STATIC_FILES SET IS_AUTO_GEN_ENABLED=" + (enabled?1:0) + " WHERE UPPER(APP_CODE)=UPPER('" + appCode + "') AND UPPER(FILE_NAME)=UPPER('" + dataFileName + "')";
   		super.execute(sql);
    }
    
    /**
     * Поменять режим генерации файла( в черновик/в чистовик)
     * @param appCode
     * @param dataFileName
     * @param enabled
     */
    public void changeStaticFileGenerationMode(String appCode,String dataFileName,boolean enabled) {
   		String sql = "UPDATE SYNC_CACHE_STATIC_FILES SET GENERATION_MODE=" + (enabled?1:0) + " WHERE UPPER(APP_CODE)=UPPER('" + appCode + "') AND UPPER(FILE_NAME)=UPPER('" + dataFileName + "')";
   		super.execute(sql);
    }
    
    /**
     * Добавить пользователя к группе контроллеров контента 
     * @param appCode
     * @param email
     */
    public void addUserToDraftGroup(String appCode,String email) {
   		String sql = "exec ADD_USER_TO_DRAFT_GROUP '" + appCode + "','" + email + "'";
   		super.execute(sql);
    }
    
    /**
     * Удалить пользователя из группы контроллеров контента 
     * @param appCode
     * @param email
     */
    public void removeUserFromDraftGroup(String appCode,String email) {
   		String sql = "exec REMOVE_USER_FROM_DRAFT_GROUP '" + appCode + "','" + email + "'";
   		super.execute(sql);
    }
    
    /**
     * Изменить статус файла генерации
     * @param appCode
     * @param fileId
     * @param publishFileMd5
     * @param draftFileMd5
     */
    public void changeStaticFileStatus(String appCode, String fileId, String publishFileMd5, String draftFileMd5) {
   		String sql = "exec SP_CHANGE_APP_FILE_STATUS '" + (appCode!=null?appCode:"") + "','" + fileId  + "'," + (publishFileMd5!= null?("'" +  publishFileMd5 + "'"):"NULL") + "," + (draftFileMd5!= null?("'" +  draftFileMd5 + "'"):"NULL");
   		super.execute(sql);
    }
    
    /**
     * Опубликовать текущий черновик
     * @param appCode
     * @param fileId
     */
    public void publishCurrentStaticFileDraft(String appCode,String fileId) {
   		String sql = "exec SP_PUBLISH_CURRENT_DRAFT '" + appCode + "','" + fileId  + "'";
   		super.execute(sql);
    }

    /**
     * Удалить текущий черновик
     * @param appCode
     * @param fileId
     */
    public void deleteCurrentStaticFileDraft(String appCode,String fileId) {
   		String sql = "exec SP_DELETE_CURRENT_DRAFT '" + appCode + "','" + fileId  + "'";
   		super.execute(sql);
    }
    
    /**
     * Получить контроллеров контента
     */
    public List<String[]> getUsersFromDraftGroup() {
    	String sql = "exec SP_GET_GENERATOR_USER_GROUPS_FOR_DRAFT";
    	return jdbcTemplate.query(sql, new RowMapper<String[]>() {

            @Override
            public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {
            	return new String[] {rs.getString("APP_ID"),rs.getString("EMAIL") };
            }

        });
    }

    /**
     * Определить режим генерации файлов (драфт/чистовик)
     * @param appCode
     * @param fileId
     * @return
     */
    public boolean getDraftGenertionModeForFile(String appCode,String fileId) {
   		String sql = "exec GET_FILE_DRAFT_STATUS '" + (appCode!=null?appCode:"") + "','" + fileId  + "'";
   		int result = super.getIntValue(sql); 
   		return (result == 1);
    }
    

    
    public Object[] getLogsFor(String fileName, Date date, boolean debug, int startIndex, int numberOfRecords) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String sql = "exec SP_SYNC_GET_GENERATION_LOGS '"+fileName+"',"+(date==null?"null":"'"+sdf.format(date)+"'")+","+(debug?1:0)+","+startIndex+","+numberOfRecords;		
		return super.getMultipleResult(sql, new ResultSetExtractor<List<GenerationLogMsg>>() {

			@Override
			public List<GenerationLogMsg> extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				List<GenerationLogMsg> result = new ArrayList<GenerationLogMsg>();
				while (rs.next()) {
					result.add(new GenerationLogMsg(rs));
				}
				return result;
			}
		}, new ResultSetExtractor<Integer>() {
			@Override
			public Integer extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				rs.next();
				return rs.getInt(1);
			}
		});
    }
        
	
}
