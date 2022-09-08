package one.paro.sharepicture;

public enum Method {
    /**
     * Request:
     * <pre>{@code
     * String method = LIST.name();
     * }</pre>
     * Response:
     * <pre>{@code
     * String status = OK.name();
     * int length;
     * String[] filenames[length];
     * }</pre>
     * or,
     * <pre>{@code
     * String status = ERROR.name();
     * }</pre>
     * and closes the connection.
     */
    LIST,
    /**
     * Request:
     * <pre>{@code
     * String method = SHOW.name();
     * String filename;
     * }</pre>
     * Response:
     * <pre>{@code
     * String status = OK.name();
     * long length;
     * byte[] data[length];
     * }</pre>
     * or,
     * <pre>{@code
     * String status = NOT_FOUND.name();
     * }</pre>
     */
    SHOW,
    /**
     * Syntax:
     * <pre>{@code
     * String opcode = CLOSE.name();
     * }</pre>
     * Closes the connection.
     */
    CLOSE
}
