package sit.int204.mobileshop.services;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sit.int204.mobileshop.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    public JwtUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByUsernameOrEmail(username)
//                .orElseThrow(() -> new UsernameNotFoundException(username));
//        return new AuthUserDetail(user.getId(), user.getUsername()
//                , user.getPassword(), getAuthorities(user.getRoles()));
        return null;
    }

    public void loadUserById(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User id " + id + " does not exist"));
    }

    public static List<GrantedAuthority> getAuthorities(String rolesAsCommaSeparated) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Arrays.asList(rolesAsCommaSeparated.split(","))
                .forEach(role -> authorities.add(getAuthority(role)));
        return authorities;
    }

    private static GrantedAuthority getAuthority(String role) {
        return new SimpleGrantedAuthority(role);
    }

}
