package ru.sberbank.syncserver2.service.generator.single.data;

/**
 * Created by sbt-kozhinsky-lb on 31.07.14.
 */
public class ETLCheckError {
    private int errorCode;
    private String errorDescription;

    public ETLCheckError(int errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getErrorDescription() + "(" + getErrorCode()+")";
	}
    
    
}
