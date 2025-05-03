package sit.int204.mobileshop.exceptions;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "timestamp", "status", "error", "message", "path" })
public class MyErrorResponse {
    private Instant timestamp = Instant.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    @JsonIgnore
    private String stackTrace;

}
