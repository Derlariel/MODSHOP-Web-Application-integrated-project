package sit.int204.mobileshop.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import sit.int204.mobileshop.exceptions.ItemNotFoundException;
import sit.int204.mobileshop.exceptions.MyErrorResponse;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<MyErrorResponse> handleItemNotFoundException(ItemNotFoundException e,
                                                                       HttpServletRequest request){
        MyErrorResponse myErrorResponse = new MyErrorResponse(
                HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage(), request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(myErrorResponse);
    }
}
