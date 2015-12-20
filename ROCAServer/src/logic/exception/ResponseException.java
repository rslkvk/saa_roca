package logic.exception;

import javax.ws.rs.core.Response;

/**
 * Created by resul on 09/06/15.
 */
public class ResponseException extends Exception {

    public ResponseException() {super();}

    public ResponseException(String message) {
        super(message);
    }
}
