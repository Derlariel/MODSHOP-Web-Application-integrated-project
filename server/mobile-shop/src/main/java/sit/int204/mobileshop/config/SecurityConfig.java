package sit.int204.mobileshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
@Bean
public PasswordEncoder passwordEncoder() {
    String idForEncode = "argon2";
    Map<String, PasswordEncoder> encodes = new HashMap<>();
    encodes.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
    encodes.put("bcrypt", new BCryptPasswordEncoder());

    DelegatingPasswordEncoder pe = new DelegatingPasswordEncoder(idForEncode,encodes);
    pe.setDefaultPasswordEncoderForMatches(encodes.get("bcrypt"));
    return pe;
}
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }


}
