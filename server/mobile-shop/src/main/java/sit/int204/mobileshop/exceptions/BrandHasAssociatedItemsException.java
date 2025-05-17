package sit.int204.mobileshop.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BrandHasAssociatedItemsException extends RuntimeException {
    private final Integer brandId;
    private final Integer itemCount;

    public BrandHasAssociatedItemsException(Integer brandId, Integer itemCount) {
        super("Cannot delete brand with ID " + brandId + " because it has " + itemCount + " associated sale items");
        this.brandId = brandId;
        this.itemCount = itemCount;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public Integer getItemCount() {
        return itemCount;
    }
}