package sit.int204.mobileshop.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(description = "Paginated response containing data items and pagination metadata")
@Getter
@Setter
@NoArgsConstructor
public class PageDto<T> {
    @Schema(description = "List of content items for the current page")
    private List<T> content;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private Boolean last;
    
    @Schema(description = "Whether this is the first page", example = "true")
    private Boolean first;
    
    @Schema(description = "Total number of pages", example = "6")
    private Integer totalPages;
    
    @Schema(description = "Number of items per page", example = "10")
    private Integer size;
    
    @Schema(description = "Current sort order", example = "price: DESC, createdOn: ASC")
    private String sort;
    
    @Schema(description = "Current page number (zero-based)", example = "0")
    private Integer page;
    
    @Schema(description = "Total number of elements across all pages", example = "42")
    private Integer totalElements;
}
