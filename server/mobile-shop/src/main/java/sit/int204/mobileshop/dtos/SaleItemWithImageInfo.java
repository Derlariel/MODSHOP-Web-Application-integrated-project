package sit.int204.mobileshop.dtos;

import lombok.Data;

import java.util.List;

@Data
public class SaleItemWithImageInfo {
    private SaleItemRequestDto saleItem;
    private List<SaleItemImageRequest> imageInfos;
}
