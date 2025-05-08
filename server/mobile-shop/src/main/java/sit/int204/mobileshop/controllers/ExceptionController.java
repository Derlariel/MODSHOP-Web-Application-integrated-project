package sit.int204.mobileshop.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import sit.int204.mobileshop.exceptions.DatabaseCommunicationException;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.exceptions.MyErrorResponse;

import java.sql.SQLException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<MyErrorResponse> handleItemNotFoundException(ItemNotFoundException e,
                                                                       HttpServletRequest request) {
        MyErrorResponse myErrorResponse = new MyErrorResponse(
                HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage(), request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myErrorResponse);
    }

    @ExceptionHandler({DataAccessException.class, SQLException.class, CannotCreateTransactionException.class, DatabaseCommunicationException.class})
    public ResponseEntity<MyErrorResponse> handleDatabaseError(Exception e, HttpServletRequest request) {
        String errorMessage = e instanceof DatabaseCommunicationException ? e.getMessage() : "Database Communication Error";

        MyErrorResponse myErrorResponse = new MyErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                errorMessage,
                request.getRequestURI()
        );
        myErrorResponse.setStackTrace(e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(myErrorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MyErrorResponse> handleUnexpectedError(Exception e , HttpServletRequest request ){
        MyErrorResponse myErrorResponse = new MyErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Something went wrong, please try again later.",
                request.getRequestURI()
        );
        myErrorResponse.setStackTrace(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(myErrorResponse);
    }

   @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MyErrorResponse> handleValidationError(MethodArgumentNotValidException ex , HttpServletRequest request){
    String message = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

    MyErrorResponse myErrorResponse = new MyErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(myErrorResponse);

    }
}
