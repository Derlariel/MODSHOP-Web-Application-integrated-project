package sit.int204.mobileshop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class DatabaseCommunicationException extends RuntimeException {
    public DatabaseCommunicationException(String message) {
        super(message);
    }
}