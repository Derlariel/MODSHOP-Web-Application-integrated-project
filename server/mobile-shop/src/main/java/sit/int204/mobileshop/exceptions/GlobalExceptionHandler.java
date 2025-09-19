package sit.int204.mobileshop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status;
        String error;

        // Map specific error messages to appropriate HTTP status codes
        if (message.contains("No access token") || message.contains("Invalid Token")) {
            status = HttpStatus.UNAUTHORIZED;
            error = "Unauthorized";
        } else if (message.contains("not active") || message.contains("not matched")) {
            status = HttpStatus.FORBIDDEN;
            error = "Forbidden";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "Internal Server Error";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        response.put("path", "/v2/users");

        return ResponseEntity.status(status).body(response);
    }
}