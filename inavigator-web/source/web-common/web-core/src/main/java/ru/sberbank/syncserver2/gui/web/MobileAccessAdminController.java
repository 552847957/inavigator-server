package ru.sberbank.syncserver2.gui.web;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.sberbank.syncserver2.gui.data.MisDictionary;
import ru.sberbank.syncserver2.gui.data.MisRole;
import ru.sberbank.syncserver2.gui.data.MisUser;
import ru.sberbank.syncserver2.gui.data.ServerRequestResult;
import ru.sberbank.syncserver2.gui.util.JSONResponseHelper;
import ru.sberbank.syncserver2.gui.web.validator.Error;
import ru.sberbank.syncserver2.gui.web.validator.UserValidator;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.datamappers.DatapowerDataResponseHandler;
import ru.sberbank.syncserver2.service.datamappers.DatapowerObjectMapperRequester;
import ru.sberbank.syncserver2.service.datamappers.DatapowerResponseMapper;
import ru.sberbank.syncserver2.service.datamappers.DatapowerResultObjectListHandler;
import ru.sberbank.syncserver2.service.ldap.LdapGroupManagementService;
import ru.sberbank.syncserver2.service.log.CSVImpl;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments.Argument;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@Controller

public class MobileAccessAdminController {

	// http://localhost:9090/web-syncserver/gui/mobile-access-admin/user/list
	// http://localhost:9090/web-syncserver/gui/mobile-access-admin/user/info?userId=15
	// http://localhost:9090/web-syncserver/gui/mobile-access-admin/role/list
	// http://localhost:9090/web-syncserver/gui/mobile-access-admin/role/listByUser?userId=15

	private static final String CURRENT_DB_SESSION_ATTR_NAME = "CURRENT_DB";


	private static Map<String,String> dictionaryProcedures = new HashMap<String, String>();

	static {
		dictionaryProcedures.put("terrbanks","TERRBANKS");
		dictionaryProcedures.put("units","UNITS");
		dictionaryProcedures.put("blocks","BLOCKS");
	}


	private DataPowerService getDatapowerService() {
		return (DataPowerService)ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerService.class);
	}

	@RequestMapping("/main.mobaccess.gui")
	public String initForm(HttpServletRequest request, HttpServletResponse response) {

		return "mobileAccessAdmin";
	}

	/**
	 * Преобразовать список MisUser объектов к множеству Long идентификаторов ролей
	 * @param roles
	 * @return
	 */
	private static Set<Long> rolesListToLongList(List<MisRole> roles) {
		Set<Long> resultRoles = new HashSet<Long>();
		for(MisRole role:roles)
			resultRoles.add(role.getRoleId());
		return resultRoles;
	}

	/**
	 * Преобразовать множество Long-идентификаторов к строке с разделителем ','
	 * @param set
	 * @return
	 */
	public static String setToString(Set<Long> set) {
		String result = "";
		for(Long element:set) {
			result = result + "," + element;
		}
		return (result.length() > 0)?result.substring(1):"";
	}

	/**
	 * Преобразовать входной параметр к корректной кодировке
	 * @param paramValue
	 * @return
	 */
	private static String decodeParameter(String paramValue) {
	  try {
		return paramValue; //new String(paramValue.getBytes("iso-8859-1"),"UTF-8");
	  } catch (Exception ex) {
		  ex.printStackTrace();
	  }
	  return null;
	}

	public void updateUser(final MisUser user,final List<Error> errors,HttpServletRequest request,final HttpServletResponse response) throws IOException {
		try {
			// далее идет логика сохранения пользвоателя в БД
			List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
			arguments.add(new Argument(1, FieldType.NUMBER, user.getUserId()!=null?(""+user.getUserId()):null));
			arguments.add(new Argument(2, FieldType.STRING, user.getIp()));
			arguments.add(new Argument(3, FieldType.STRING, user.getUserName()));
			arguments.add(new Argument(4, FieldType.STRING, user.getBusinessUnitId()));
			arguments.add(new Argument(5, FieldType.STRING, user.getEmail()));
			arguments.add(new Argument(6, FieldType.STRING, user.getPosition()));
			arguments.add(new Argument(7, FieldType.NUMBER, (user.getTerrbankId()== null || user.getTerrbankId().equals(""))?null:user.getTerrbankId()));
			arguments.add(new Argument(8, FieldType.NUMBER, (user.getBusinessBlockId()== null || user.getBusinessBlockId().equals(""))?null:user.getBusinessBlockId()));
			arguments.add(new Argument(9, FieldType.STRING, user.getComment()));
			arguments.add(new Argument(10, FieldType.STRING,user.getEmailAD()));

			//  Этап 1. сохранение данных пользователя в БД Альфа
			new DatapowerObjectMapperRequester<MisUser>().request(
					"MIS_BASE2.UPDATE_USER",
					arguments,
					getCurrentDb(request),
					getDatapowerService(),
					new DatapowerDataResponseHandler() {
						@Override
						public void handleDataResponse(DataResponse dataset) throws IOException {
							if (dataset.getResult() != Result.OK)
								errors.add(new Error("", "Ошибка запроса в базу данных при сохранении пользователя.",true));
							else {
								try {
									if (dataset.getMetadata().getFields().get(0).getName().equals("error")) {
										for (DatasetRow row:dataset.getDataset().getRows()) {
												errors.add(new Error("",row.getValues().get(0),true));
										}
									} else
										user.setUserId(Long.valueOf(dataset.getDataset().getRows().get(0).getValues().get(0)));

								} catch (Exception ex)  {
									errors.add(new Error("", "Ошибка запроса в базу данных при сохранении пользователя.",true));
									ex.printStackTrace();
								}
							}
						}
					});

			// Этап 2. Подготовка ролей к удалению и добавлению
			// 2.1 Получаем список навигаторских ролей
			// 2.2 Получаем список ролей пользователя
			// 2.3 высчитываем разницу userRoles - inavRoles - это ненавигаторские роли (которые мы должны при сохранении обязательно не потерять)
			Set<Long> deletedRoles = new HashSet<Long>();
			Set<Long> addedRoles = new HashSet<Long>();

			Set<Long> inavRoleIds = rolesListToLongList(getInavRoles(request));
			Set<Long> oldUserRoleIds = rolesListToLongList(getUserRoles(request,user.getUserId()!=null?(""+user.getUserId()):null));
			Set<Long> newUserRoleIds = new HashSet<Long>(user.getRoles());

			for (Long userRoleId:oldUserRoleIds) {
				// если роль навигаторская, но в новом списке ее нет, то считаем ее удаленной
				if (inavRoleIds.contains(userRoleId) && !newUserRoleIds.contains(userRoleId))
					deletedRoles.add(userRoleId);
			}
			for (Long userRoleId:newUserRoleIds) {
				// если роль навигаторская, но в старом списке ее нет, то считаем ее добавленной
				if (inavRoleIds.contains(userRoleId) && !oldUserRoleIds.contains(userRoleId))
					addedRoles.add(userRoleId);
			}

			// если есть удаленная или добавленная роль, то производим вызов
			if (deletedRoles.size() > 0 || addedRoles.size() > 0) {
				arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
				arguments.add(new Argument(1, FieldType.NUMBER, user.getUserId()!=null?(""+user.getUserId()):null));
				arguments.add(new Argument(2, FieldType.STRING, setToString(deletedRoles)));
				arguments.add(new Argument(3, FieldType.STRING, setToString(addedRoles)));

				new DatapowerObjectMapperRequester<MisUser>().request(
						"MIS_BASE2.UPDATE_USER_ROLES",
						arguments,
						getCurrentDb(request),
						getDatapowerService(),
						new DatapowerDataResponseHandler() {
							@Override
							public void handleDataResponse(DataResponse dataset) throws IOException {
								if (dataset.getResult() != Result.OK)
									errors.add(new Error("", "Ошибка запроса в БД при сохранении ролей.",true));
							}
				});
			}

			// Прописыванием прав в MDM
			if (errors.isEmpty() && user.isHasMdmRights()) {
				try {
					((LdapGroupManagementService)ServiceManager.getInstance().findFirstServiceByClassCode(LdapGroupManagementService.class)).addInavGroupToUser(user.getEmail());
				} catch (Exception ex) {
					errors.add(new Error("", "Ошибка сохранения прав в MDM.",false));
				}
			}
		} finally {
			JSONResponseHelper.singleObjectToJsonHttpOutput(new ServerRequestResult((errors.size() == 0)?null:errors), response);
		}
	}

	/**
	 * Получить список ролей пользователя БД
	 * @param userId
	 * @return
	 * @throws IOException
	 */
	public List<MisRole> getUserRoles(HttpServletRequest request,String userId) throws IOException{
		final List<MisRole> userRoles = new ArrayList<MisRole>();

		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.NUMBER,userId));

		new DatapowerObjectMapperRequester<MisRole>().request(
				"MIS_BASE2.GET_USER_ROLES",
				arguments,
				getCurrentDb(request),
				getDatapowerService(),
				new DatapowerResponseMapper<MisRole>() {

					@Override
					public MisRole convertResultToObject(DatasetRow row) {
						MisRole role = new MisRole();
						role.setRoleId(Long.valueOf(row.getValues().get(0)));
						return role;
					}

				},
				new DatapowerResultObjectListHandler<MisRole>() {
					@Override
					public void handleResultObjectList(List<MisRole> results) throws IOException {
							userRoles.addAll(results);
					}
				});

		return userRoles;
	}

	/**
	 * Получить список ролей навигатора
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public List<MisRole> getInavRoles(HttpServletRequest request) throws IOException {
		final List<MisRole> inavRoles = new ArrayList<MisRole>();
		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		new DatapowerObjectMapperRequester<MisRole>().request(
				"MIS_BASE2.GET_INAV_ROLES",
				arguments,
				getCurrentDb(request),
				getDatapowerService(),
				new DatapowerResponseMapper<MisRole>() {

					@Override
					public MisRole convertResultToObject(DatasetRow row) {
						MisRole role = new MisRole();
						role.setRoleId(Long.valueOf(row.getValues().get(0)));
						role.setRoleName(row.getValues().get(1));
						role.setRoleDescription(row.getValues().get(2));
						role.setRoleTypeDescription(row.getValues().get(3));
						role.setRoleTypeId(Long.valueOf(row.getValues().get(4)));
						return role;
					}

				},
				new DatapowerResultObjectListHandler<MisRole>() {
					@Override
					public void handleResultObjectList(List<MisRole> results) throws IOException {
						inavRoles.addAll(results);
					}
				});

		return inavRoles;
	}

	@RequestMapping(value="/mobile-access-admin/user/info/put",method = RequestMethod.POST,produces = "application/json")
	public void updateUser(HttpServletRequest request,HttpServletResponse response,@RequestBody String info) throws IOException {
		final List<Error> errors = new ArrayList<Error>();
		MisUser user = new ObjectMapper().readValue(info, MisUser.class);
		new UserValidator().validate(user,errors);
		if (!errors.isEmpty()) {
			JSONResponseHelper.singleObjectToJsonHttpOutput(new ServerRequestResult(errors), response);
		} else {
			updateUser(user, errors,request, response);
		}
	}

	@RequestMapping(value="/mobile-access-admin/db/get")
	public void queryCurrentDb(HttpServletRequest request,HttpServletResponse response) throws IOException {
		JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(response.getWriter());
		jsonGenerator.setCodec(new ObjectMapper());

		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringField("currentDb", getCurrentDb(request));
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();

	}

	@RequestMapping(value="/mobile-access-admin/db/set")
	public void setCurrentDb(HttpServletRequest request,HttpServletResponse response) throws IOException {
		if (request.getParameter("db") != null)
			request.getSession().setAttribute(CURRENT_DB_SESSION_ATTR_NAME, request.getParameter("db"));

		response.getOutputStream().write("{}".getBytes());
	}

	/**
	 * Получить текущее значение БД из сессии
	 * @param request
	 * @return
	 */
	private String getCurrentDb(HttpServletRequest request) {
		 if (request.getSession().getAttribute(CURRENT_DB_SESSION_ATTR_NAME) == null)
			 request.getSession().setAttribute(CURRENT_DB_SESSION_ATTR_NAME, "finik2-new");
		return (String)request.getSession().getAttribute(CURRENT_DB_SESSION_ATTR_NAME);
	}

	/**
	 * Получить список пользователей
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/mobile-access-admin/user/list")
	public void getUserList(HttpServletRequest request, final HttpServletResponse response) throws IOException {
		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.STRING,request.getParameter("userName") != null ?decodeParameter(request.getParameter("userName")): "" ));
		arguments.add(new Argument(2, FieldType.STRING,request.getParameter("ip") != null ?request.getParameter("ip"): "" ));
		arguments.add(new Argument(3, FieldType.STRING,request.getParameter("terrbank") != null ?request.getParameter("terrbank"): "" ));
		arguments.add(new Argument(4, FieldType.STRING,request.getParameter("business") != null ?request.getParameter("business"): "" ));
		arguments.add(new Argument(5, FieldType.NUMBER,request.getParameter("navRoles") != null ?("true".equalsIgnoreCase(request.getParameter("navRoles"))?"1":"0"):"0"));
		arguments.add(new Argument(6, FieldType.NUMBER,"0"));

		new DatapowerObjectMapperRequester<MisUser>().request(
				"MIS_BASE2.GET_USERS_LIST",
				arguments,
				getCurrentDb(request),
				getDatapowerService(),
				new DatapowerResponseMapper<MisUser>() {

					@Override
					public MisUser convertResultToObject(DatasetRow row) {
						MisUser user = new MisUser();
						user.setUserId(Long.valueOf(row.getValues().get(0)));
						user.setIp(row.getValues().get(1));
						user.setUserName(row.getValues().get(2));
						user.setTerrbankShortName(row.getValues().get(3));
						user.setBusinessUnitId(row.getValues().get(4));
						return user;
					}

				},
				new DatapowerResultObjectListHandler<MisUser>() {
					@Override
					public void handleResultObjectList(List<MisUser> results) throws IOException {
						JSONResponseHelper.listObjectToJsonOutput(results, response);
					}
				});
	}

	/**
	 * Получить список пользователей в CSV формате
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/mobile-access-admin/user/listCSV")
	public void getUserListCSV(HttpServletRequest request, final HttpServletResponse response) throws IOException {
		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.STRING,request.getParameter("userName") != null ?decodeParameter(request.getParameter("userName")): "" ));
		arguments.add(new Argument(2, FieldType.STRING,request.getParameter("ip") != null ?request.getParameter("ip"): "" ));
		arguments.add(new Argument(3, FieldType.STRING,request.getParameter("terrbank") != null ?request.getParameter("terrbank"): "" ));
		arguments.add(new Argument(4, FieldType.STRING,request.getParameter("business") != null ?request.getParameter("business"): "" ));
		arguments.add(new Argument(5, FieldType.NUMBER,request.getParameter("navRoles") != null ?("true".equalsIgnoreCase(request.getParameter("navRoles"))?"1":"0"):"0"));
		arguments.add(new Argument(6, FieldType.NUMBER,"1"));

		new DatapowerObjectMapperRequester<MisUser>().request(
				"MIS_BASE2.GET_USERS_LIST",
				arguments,
				getCurrentDb(request),
				getDatapowerService(),
				new DatapowerResponseMapper<MisUser>() {

					@Override
					public MisUser convertResultToObject(DatasetRow row) {
						MisUser user = new MisUser();
						user.setUserId(Long.valueOf(row.getValues().get(0)));
						user.setIp(row.getValues().get(1));
						user.setUserName(row.getValues().get(2));
						user.setTerrbankShortName(row.getValues().get(3));
						user.setBusinessUnitId(row.getValues().get(4));
						return user;
					}

				},
				new DatapowerResultObjectListHandler<MisUser>() {
					@Override
					public void handleResultObjectList(List<MisUser> results) throws IOException {

						class IterableMisUser implements Iterable<String> {
							private MisUser user;
							public IterableMisUser(MisUser user) {
								this.user = user;
							}

							@Override
							public Iterator<String> iterator() {
								return new Iterator<String>() {
									private int i = 0;
									@Override
									public void remove() {
										throw new UnsupportedOperationException();
									}

									@Override
									public String next() {
										switch (i++) {
										case 0: return user.getUserName();
										case 1: return user.getIp();
										case 2: return user.getTerrbankShortName();
										case 3: return user.getBusinessUnitId();
										default: return "";
										}
									}

									@Override
									public boolean hasNext() {
										return i<4;
									}
								};
							}
						}
						List<IterableMisUser> list = new ArrayList<IterableMisUser>(results.size());
						MisUser columnNames = new MisUser();
						columnNames.setUserName("userName");
						columnNames.setIp("ip");
						columnNames.setTerrbankShortName("terrbankShortName");
						columnNames.setBusinessUnitId("businessUnitId");
						list.add(new IterableMisUser(columnNames));
						for (MisUser user: results) {
							list.add(new IterableMisUser(user));
						}

						CSVImpl CSVGenerator = new CSVImpl("MISUsers.csv");
						CSVGenerator.generateFile(response, list);

					}
				});
	}

	@RequestMapping("mobile-access-admin/role/list")
	public void getRolesList(HttpServletRequest request,final HttpServletResponse response) throws IOException {
		List<MisRole> inavRoles = getInavRoles(request);
		JSONResponseHelper.listObjectToJsonOutput(inavRoles, response);
	}

	@RequestMapping("mobile-access-admin/role/listByUser")
	public void getUserRolesList(HttpServletRequest request,final HttpServletResponse response) throws IOException {
		List<MisRole> userRoles = getUserRoles(request,request.getParameter("userId") != null ?request.getParameter("userId"): "");
		JSONResponseHelper.listObjectToJsonOutput(userRoles, response);
	}

	@RequestMapping("mobile-access-admin/user/info")
	public void getUserById(HttpServletRequest request,final HttpServletResponse response) throws IOException {

		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.NUMBER,request.getParameter("userId") != null ?request.getParameter("userId"): "" ));

		new DatapowerObjectMapperRequester<MisUser>().request(
				"MIS_BASE2.GET_USER_INFO",
				arguments,
				getCurrentDb(request),
				getDatapowerService(),
				new DatapowerResponseMapper<MisUser>() {

					@Override
					public MisUser convertResultToObject(DatasetRow row) {
						MisUser user = new MisUser();
						user.setUserId(Long.valueOf(row.getValues().get(0)));
						user.setIp(row.getValues().get(1));
						user.setUserName(row.getValues().get(2));
						user.setBusinessUnitId(row.getValues().get(3));
						user.setEmail(row.getValues().get(4));
						user.setPosition(row.getValues().get(5));
						user.setTerrbankId(row.getValues().get(6));
						user.setBusinessBlockId(row.getValues().get(7));
						user.setComment(row.getValues().get(8));
						user.setCreateDate(row.getValues().get(9));
						user.setEmailAD(row.getValues().get(10));

						boolean hasInavLdapGroup = false;
						try {
							hasInavLdapGroup = ((LdapGroupManagementService)ServiceManager.getInstance().findFirstServiceByClassCode(LdapGroupManagementService.class)).hasInavUserGroup(user.getEmail());
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						user.setHasMdmRights(hasInavLdapGroup);

						return user;
					}
				},
				new DatapowerResultObjectListHandler<MisUser>() {
					@Override
					public void handleResultObjectList(List<MisUser> results) throws IOException {
						JSONResponseHelper.listObjectToJsonOutput(results, response);
					}
				});
	}


	@RequestMapping("mobile-access-admin/dictionary/{dictionaryId}/list")
	public void getUserById(@PathVariable String dictionaryId,HttpServletRequest request,final HttpServletResponse response) throws IOException {
		new DatapowerObjectMapperRequester<MisDictionary>().request(
				"MIS_BASE2.GET_DICTIONARY_" + dictionaryProcedures.get(dictionaryId),
				new ArrayList<OnlineRequest.Arguments.Argument>(),
				getCurrentDb(request),
				getDatapowerService(),
				new DatapowerResponseMapper<MisDictionary>() {

					@Override
					public MisDictionary convertResultToObject(DatasetRow row) {
						MisDictionary dictionary = new MisDictionary();
						dictionary.setId(row.getValues().get(0));
						dictionary.setName(row.getValues().get(1));
						return dictionary;
					}
				},
				new DatapowerResultObjectListHandler<MisDictionary>() {
					@Override
					public void handleResultObjectList(List<MisDictionary> results) throws IOException {
						JSONResponseHelper.listObjectToJsonOutput(results, response);
					}
				});
	}


}
