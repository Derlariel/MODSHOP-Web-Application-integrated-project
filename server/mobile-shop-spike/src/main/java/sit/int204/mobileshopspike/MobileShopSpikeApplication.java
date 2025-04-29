package sit.int204.mobileshopspike;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sit.int204.mobileshopspike.utils.ListMapper;

@SpringBootApplication
public class MobileShopSpikeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileShopSpikeApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public ListMapper listMapper(){
        return ListMapper.getInstance();
    }
}
