package sit.int204.mobileshop.exceptions;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyErrorResponse {
    private Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String message;
    private final String instance;
    @JsonIgnore
    private String stackTrace;

}
