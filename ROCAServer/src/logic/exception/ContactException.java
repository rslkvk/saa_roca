package logic.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by resul on 09/06/15.
 */
public class ContactException extends Exception {
    public ContactException() {super();}
    public ContactException(String message) {
        super(message);
    }
}
