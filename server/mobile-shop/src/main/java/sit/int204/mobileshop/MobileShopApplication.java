package sit.int204.mobileshop;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.utils.ListMapper;

@SpringBootApplication
public class MobileShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileShopApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(SaleItem.class, SaleItemDetailDto.class).addMappings(mapper -> {
            mapper.map(src -> src.getBrand().getName(), SaleItemDetailDto::setBrandName);
        });

        return modelMapper;
    }


    @Bean
    public ListMapper listMapper(){
        return ListMapper.getInstance();
    }
}
