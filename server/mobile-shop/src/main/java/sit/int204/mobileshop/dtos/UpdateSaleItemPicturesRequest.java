package sit.int204.mobileshop.dtos;

import java.util.List;

public class UpdateSaleItemPicturesRequest {
    private List<Integer> deleteIds;
    private List<ImageOrderDto> order;

    public List<Integer> getDeleteIds() {
        return deleteIds;
    }
    public void setDeleteIds(List<Integer> deleteIds) {
        this.deleteIds = deleteIds;
    }
    public List<ImageOrderDto> getOrder() {
        return order;
    }
    public void setOrder(List<ImageOrderDto> order) {
        this.order = order;
    }

    public static class ImageOrderDto {
        private Integer imageId;
        private Integer position;

        public Integer getImageId() { return imageId; }
        public void setImageId(Integer imageId) { this.imageId = imageId; }
        public Integer getPosition() { return position; }
        public void setPosition(Integer position) { this.position = position; }
    }
}
