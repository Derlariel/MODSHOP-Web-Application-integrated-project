package sit.int204.mobileshop.dtos;

import java.util.List;

public class UpdateSaleItemPicturesRequest {
    private List<Integer> imageOrder;
    private List<Integer> imagesToDelete;

    public UpdateSaleItemPicturesRequest() {}

    public UpdateSaleItemPicturesRequest(List<Integer> imageOrder, List<Integer> imagesToDelete) {
        this.imageOrder = imageOrder;
        this.imagesToDelete = imagesToDelete;
    }

    public List<Integer> getImageOrder() {
        return imageOrder;
    }

    public void setImageOrder(List<Integer> imageOrder) {
        this.imageOrder = imageOrder;
    }

    public List<Integer> getImagesToDelete() {
        return imagesToDelete;
    }

    public void setImagesToDelete(List<Integer> imagesToDelete) {
        this.imagesToDelete = imagesToDelete;
    }
}
