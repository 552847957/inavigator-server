package ru.sberbank.syncserver2.service.core;

/**
 * @author Sergey Erin
 *
 */
public class ComponentException extends Exception {

    /**
     *
     */
    public ComponentException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public ComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ComponentException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ComponentException(Throwable cause) {
        super(cause);
    }

}
