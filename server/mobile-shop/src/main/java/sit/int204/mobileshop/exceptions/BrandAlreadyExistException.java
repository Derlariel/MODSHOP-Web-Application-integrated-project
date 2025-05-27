package sit.int204.mobileshop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BrandAlreadyExistException extends RuntimeException {
    public BrandAlreadyExistException(String message) {
        super(message);
    }

}
