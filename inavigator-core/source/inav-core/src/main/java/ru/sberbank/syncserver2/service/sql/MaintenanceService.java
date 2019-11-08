package ru.sberbank.syncserver2.service.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MaintenanceService extends SingleThreadBackgroundService implements SQLService, PublicService {
	private SQLService originalService;
	private String suffixUrl = "/monitor-sigma/gui/status.maintenance";

	/**
	 * Текущая стратегия обработки запроса.
	 * Так должно работать быстрее, чем при каждом запросе проверять остановлен ли сервис
	 */
	private volatile ServiceStrategy strategy;

	private String[] hosts = new String[0];


	public MaintenanceService() {
		super(30);
	}

	@Override
	public void doInit() {
		strategy = new SwitchedOff();

	}

	@Override
	public void doRun() {
		StatusInfo loadedInfo = null;
		Exception exception = null;
		for (String host: hosts) {
			if (host != null && host.trim().length() > 0) {
				try {
					loadedInfo = getStatusFromHost(host.trim());
					if (loadedInfo != null)
						break;

				} catch (Exception e) {
					exception = e;
				}
			}
		}

		// обновляем статус
		if (loadedInfo != null)
			refreshStatus(loadedInfo);
		else if (exception != null) {
			tagLogger.log("Ошибка при обновлении статуса: " + exception);
			logger.error(exception, exception);
		}

	}

	private void refreshStatus(StatusInfo newStatus) {
		if (strategy.refreshStatus(newStatus))
			return;

		// меняем стратегию
		if (newStatus.active)
			strategy = new SwitchedOn(newStatus);
		else
			strategy = new SwitchedOff();
	}

	private StatusInfo getStatusFromHost(String host) throws Exception {
		URL url = new URL(host + suffixUrl);
		Object[] results = new ObjectMapper().readValue(url, Object[].class);

		if (!(Boolean)results[0])
			return new StatusInfo();
		if (results.length != 4)
			throw new IllegalArgumentException(host + " returned wrong number of arguments");

		String text = results[1].toString();
		String emails = results[2].toString();
		Long date = Long.valueOf(results[3].toString());

		return new StatusInfo(text, date, emails);
	}

	@Override
	public void request(HttpServletRequest request, HttpServletResponse response) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataResponse request(OnlineRequest request) {
		return strategy.request(request);
	}


	public void setOriginalService(SQLService originalService) {
		this.originalService = originalService;
	}


	public void setHosts(String hosts) {
		this.hosts = hosts.trim().split(";");
	}


	/**
	 * реализация стратегии при выключенном режиме
	 *
	 */
	private class SwitchedOff extends ServiceStrategy {
		public SwitchedOff() {
			tagLogger.log("Режим тех. поддержки выключен");
		}

		@Override
		public DataResponse request(OnlineRequest request) {
			return originalService.request(request);
		}

		@Override
		boolean refreshStatus(StatusInfo newStatusInfo) {
			if (newStatusInfo.active)
				return false;
			return true;
		}
	}

	/**
	 * реализация стратегии при включенном режиме
	 *
	 */
	private class SwitchedOn extends ServiceStrategy {
		private volatile StatusInfo statusInfo = null;
		public SwitchedOn(StatusInfo statusInfo) {
			if (!statusInfo.active)
				throw new IllegalArgumentException("Can't run maintenance strategy with inactive status");
			tagLogger.log("Режим тех. поддержки включен");
			this.statusInfo = statusInfo;
		}

		@Override
		public DataResponse request(OnlineRequest request) {
			if (statusInfo.isController(request.getUserEmail()))
				return originalService.request(request);
			return statusInfo.response;
		}

		@Override
		boolean refreshStatus(StatusInfo newStatusInfo) {
			if (!newStatusInfo.active)
				return false;
			if (!statusInfo.dateChanged.equals(newStatusInfo.dateChanged))
				statusInfo = newStatusInfo; // обновляем состояние
			return true;
		}
	}

	private abstract class ServiceStrategy implements SQLService {
		/**
		 * обновить состояние. Веруть false если новое состояние невозможно для этой стратегии
		 * @param newStatusInfo
		 * @return
		 */
		abstract boolean refreshStatus(StatusInfo newStatusInfo);
	}

	/**
	 * статическая информация о состоянии
	 *
	 */
	private static class StatusInfo {
		final boolean active;
		final Set<String> controllers;
		final DataResponse response;
		final Long dateChanged;

		public StatusInfo() {
			active = false;
			controllers = Collections.unmodifiableSet(Collections.EMPTY_SET);
			response = null;
			dateChanged = null;
		}

		public StatusInfo(String textMessage, Long dateChanged, String users) {
			active = true;
			response = new DataResponse();
			response.setResult(Result.FAIL_MAINTENANCE);
			response.setError(textMessage);
			this.dateChanged = dateChanged;
			Set<String> local = new HashSet<String>();
			if (users != null)
				for (String user: users.split(";")) {
					user = user.trim().toLowerCase();
					if (!user.equals("")) {
						local.add(user);
					}
				}
			// неизменяемый сет
			controllers = Collections.unmodifiableSet(local);
		}

		/**
		 * Проверить, принадлежит ли данный email к группе контроллеров
		 * @param email
		 * @return
		 */
		public boolean isController(String email) {
			if (email == null)
				return false;
			return controllers.contains(email.toLowerCase());
		}

	}

	public static void main(String[] args) throws Exception {
		URL url = new URL("http://localhost:8090/monitor-sigma/gui/status.maintenance.gui");
		/*Object[] t = new ObjectMapper().readValue(url, Object[].class);
		for (Object i: t) {
			System.out.println(i.getClass());
		}*/
	}

}
