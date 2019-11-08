package ru.sberbank.syncserver2.gui.data;

import java.util.List;
import ru.sberbank.syncserver2.gui.web.validator.Error;

public class ServerRequestResult {
	
	private boolean requestResult;
	
	private List<Error> errors;

	public ServerRequestResult(List<Error> errors) {
		super();
		this.errors = errors;
	}

	public boolean isRequestResult() {
		return requestResult;
	}

	public void setRequestResult(boolean requestResult) {
		this.requestResult = requestResult;
	}

	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}
	
	
	
	
}
