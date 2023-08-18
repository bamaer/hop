package org.apache.hop.core.graph;

import org.apache.hop.core.exception.HopException;

public class HopGraphDatabaseException extends HopException {

    public static final long serialVersionUID = 0L;

    /** Constructs a new throwable with null as its default message **/
    public HopGraphDatabaseException(){ super();}

    /** Constructs a new throwable with the specified default message.
     *
     * @param message - the detail message. The detail message is saved for later retrieval by the
     *     getMessage() method.
     *
     */
    public HopGraphDatabaseException(String message){ super(message); }

    /**
     * Constructs a new throwable with the specified cause and a detail message of (cause==null ? null
     * : cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null
     *     value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public HopGraphDatabaseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new throwable with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage()
     *     method).
     * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null
     *     value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public HopGraphDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
