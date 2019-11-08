package ru.sberbank.syncserver2.service.monitor.check;

/**
 * Created by sbt-kozhinsky-lb on 25.04.14.
 */
public class CheckResult implements ICheckResult {
    private boolean passed;
    private String errorMessage;
        
    /**
     * Необязательное поле КОД проверки 
     */
    private String code;

    public CheckResult(boolean passed, String errorMessage) {
        this.passed = passed;
        this.errorMessage = errorMessage;
    }

    public CheckResult(String code, boolean passed, String errorMessage) {
        this.code = code;
    	this.passed = passed;
        this.errorMessage = errorMessage;
    }
    
    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getCode() {
		return (code == null || code.equals("")) ? DEFAULT_CHECK_CODE : code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getErrorMessage() {
        String errorMessageForSending = errorMessage==null ? null:errorMessage.replace('\\','/');
        return errorMessageForSending;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckResult)) return false;

        CheckResult result = (CheckResult) o;

        if (passed != result.passed) return false;
        if (errorMessage != null ? !errorMessage.equals(result.errorMessage) : result.errorMessage != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (passed ? 1 : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CheckResult{" +
                "passed=" + passed +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
