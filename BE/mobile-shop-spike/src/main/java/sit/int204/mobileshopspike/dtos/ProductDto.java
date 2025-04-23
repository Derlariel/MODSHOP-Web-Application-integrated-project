package sit.int204.mobileshopspike.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto<T> {
    private Integer id;
    private String name;
    private String description;
    private Integer price;
    private String brand;


}
