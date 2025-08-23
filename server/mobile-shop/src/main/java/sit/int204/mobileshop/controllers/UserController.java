package sit.int204.mobileshop.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sit.int204.mobileshop.dtos.RegisterUserDto;
import sit.int204.mobileshop.entities.User;
import sit.int204.mobileshop.services.UserService;

@RestController
@CrossOrigin(origins = "${app.origins}")
@RequestMapping("/v2/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserDto dto) {
        User savedUser = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        return userService.verifyUser(token);
    }

}
