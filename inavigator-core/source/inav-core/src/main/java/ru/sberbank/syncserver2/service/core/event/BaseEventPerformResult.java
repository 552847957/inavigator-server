package ru.sberbank.syncserver2.service.core.event;

public class BaseEventPerformResult {
	
	private boolean isSuccess;
	
	private int resultCode;
	
	private String resultMessage;

	public BaseEventPerformResult(boolean isSuccess) {
		super();
		this.isSuccess = isSuccess;
		this.resultCode = 0;
		this.resultMessage = "OK";
	}

	public BaseEventPerformResult(boolean isSuccess, int resultCode,
			String resultMessage) {
		super();
		this.isSuccess = isSuccess;
		this.resultCode = resultCode;
		this.resultMessage = resultMessage;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	
	
	
	
	
}
