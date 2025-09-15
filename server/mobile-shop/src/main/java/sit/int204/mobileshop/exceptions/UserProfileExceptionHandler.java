package sit.int204.mobileshop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserProfileExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        
        if ("User not found, Invalid Token".equals(message) || 
            "No access token provided".equals(message)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }
        
        if ("User is not active".equals(message) || 
            "Request user id not matched with id in access token".equals(message)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
        }
        
        // Default to 500 for other runtime exceptions
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}