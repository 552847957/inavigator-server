package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.lang3.StringEscapeUtils;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.log.TagHelper;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLRequestLogService extends SingleThreadBackgroundService implements SQLService, PublicService {
	private SQLService originalService;
	private String tag = "ONLINE ERRORS";

	/**
	 * Текущая стратегия обработки запроса.
	 * Так должно работать быстрее, чем при каждом запросе проверять остановлен ли сервис
	 */
	private volatile SQLService strategy;

	private ConcurrentLinkedQueue<RequestErrorInfo> queue = new ConcurrentLinkedQueue<SQLRequestLogService.RequestErrorInfo>();

	public SQLRequestLogService() {
		super(5);
	}

	@Override
	public void doInit() {
		strategy = new LogSwitchedOn();
	}

	@Override
	public void doRun() {
		RequestErrorInfo error;
		while ((error = queue.poll()) != null) {
			TagHelper.writeToTagLogger(error.date, error.request.getUserEmail(), error.getDescription(), tag);
		}
	}

	@Override
	public void request(HttpServletRequest request, HttpServletResponse response) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataResponse request(OnlineRequest request) {
		return strategy.request(request);
	}


	@Override
	protected void doStop() {
		strategy = new LogSwitchedOff(); // подставляем другую реализацию
		super.doStop();
		queue.clear(); // очищаем очередь, т.к. ее содержимое не должно попасть при старте сервиса в следующий раз
	}

	public void setOriginalService(SQLService originalService) {
		this.originalService = originalService;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	/**
	 * реализация стратегии при выключенном логировании
	 *
	 */
	private class LogSwitchedOff implements SQLService {
		public LogSwitchedOff() {
			tagLogger.log("Логирование выключено");
		}

		@Override
		public DataResponse request(OnlineRequest request) {
			return originalService.request(request);
		}
	}

	/**
	 * реализация стратегии при включенном логировании
	 *
	 */
	private class LogSwitchedOn implements SQLService {
		public LogSwitchedOn() {
			tagLogger.log("Логирование включено");
		}

		@Override
		public DataResponse request(OnlineRequest request) {
			DataResponse response = originalService.request(request);
			if (response.getResult() != Result.OK && response.getResult() != Result.FAIL_MAINTENANCE)
				queue.add(new RequestErrorInfo(request, response.getError()));
			return response;
		}
	}

	/**
	 * информация о запросе с ошибкой
	 *
	 */
	private static class RequestErrorInfo {
		final OnlineRequest request;
		final String error;
		final Date date;

		RequestErrorInfo(OnlineRequest request, String error) {
			this.request = request;
			this.error = error;
			date = new Date();
		}

		public String getDescription() {
			return "<pre class=\"preformat\">"+StringEscapeUtils.escapeXml(XMLHelper.writeXMLToString(request, true, OnlineRequest.class))+"</pre>"+error;
		}
	}

}
