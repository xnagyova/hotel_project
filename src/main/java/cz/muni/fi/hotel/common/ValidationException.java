package cz.muni.fi.hotel.common;

/**
 *@author kkatanik & snagyova
 */
public class ValidationException extends RuntimeException {

    /**
     * Creates a new instance of
     * <code>ValidationException</code> without detail message.
     */
    public ValidationException() {
    }

    /**
     * Constructs an instance of
     * <code>ValidationException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ValidationException(String msg) {
        super(msg);
    }

}
