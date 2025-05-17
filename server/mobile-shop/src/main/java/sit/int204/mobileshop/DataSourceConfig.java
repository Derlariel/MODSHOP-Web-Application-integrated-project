package sit.int204.mobileshop;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() throws Exception {
        String password = new String(Files.readAllBytes(Paths.get("/run/secrets/db_password"))).trim();

        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/mobile_shop?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC")
                .username("root")
                .password(password)
                .build();
    }
}
