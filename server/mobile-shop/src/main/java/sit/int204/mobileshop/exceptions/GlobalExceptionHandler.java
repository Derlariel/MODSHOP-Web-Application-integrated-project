package sit.int204.mobileshop.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataAccessException.class, SQLException.class, CannotCreateTransactionException.class,
            DatabaseCommunicationException.class})
    public ResponseEntity<MyErrorResponse> handleDatabaseError(Exception e, HttpServletRequest request) {
        String errorMessage = e instanceof DatabaseCommunicationException ? e.getMessage()
                : "Database Communication Error";

        MyErrorResponse myErrorResponse = new MyErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                errorMessage,
                request.getRequestURI());
        myErrorResponse.setStackTrace(e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(myErrorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MyErrorResponse> handleUnexpectedError(Exception e,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        MyErrorResponse myErrorResponse = new MyErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Something went wrong, please try again later.",
                request.getRequestURI());
        myErrorResponse.setStackTrace(e.getMessage());
        return ResponseEntity.status(status).body(myErrorResponse);
    }
}
