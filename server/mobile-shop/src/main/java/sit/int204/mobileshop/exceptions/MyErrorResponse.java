package sit.int204.mobileshop.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Schema(description = "Standard error response structure")
@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "timestamp", "status", "error", "message", "path" })
public class MyErrorResponse {
    @Schema(description = "Timestamp when the error occurred", example = "2023-05-20T14:32:00Z")
    private Instant timestamp = Instant.now();
    
    @Schema(description = "HTTP status code", example = "400")
    private final int status;
    
    @Schema(description = "Error type", example = "Bad Request")
    private final String error;
    
    @Schema(description = "Detailed error message", example = "Brand name 'Apple' is already used.")
    private final String message;
    
    @Schema(description = "Path that caused the error", example = "/itb-mshop/v1/brands")
    private final String path;
    
    @JsonIgnore
    private String stackTrace;
}
