/**
 *
 */
package ru.sberbank.syncserver2.service.generator.single;

import java.util.List;

import ru.sberbank.syncserver2.service.generator.single.data.ETLCheckError;
/**
 * @author Yuliya Solomina
 *
 */
public class GeneratorException extends RuntimeException {
	private static final long serialVersionUID = -8013260625552623432L;
    private boolean cancelled;
    /**
     * Список ошщибок генерации
     */
    List<ETLCheckError> errors;

	public List<ETLCheckError> getErrors() {
		return errors;
	}

	public void setErrors(List<ETLCheckError> errors) {
		this.errors = errors;
	}

	public GeneratorException(String message, boolean cancelled,List<ETLCheckError> errors) {
		super(message);
        this.cancelled = cancelled;
        this.errors = errors;
	}
	/**
	 * @param message
	 */
	public GeneratorException(String message, boolean cancelled) {
		super(message);
        this.cancelled = cancelled;
	}

	/**
	 * @param cause
	 */
	public GeneratorException(Throwable cause, boolean cancelled) {
		super(cause);
        this.cancelled = cancelled;
	}



	/**
	 * @param message
	 * @param cause
	 */
	public GeneratorException(String message, Throwable cause, boolean cancelled) {
		super(message, cause);
        this.cancelled = cancelled;
	}

    public boolean isCancelled() {
        return cancelled;
    }
}
