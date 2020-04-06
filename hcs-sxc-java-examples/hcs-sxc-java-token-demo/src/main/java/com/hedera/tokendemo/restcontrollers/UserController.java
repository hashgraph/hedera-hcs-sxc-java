package com.hedera.tokendemo.restcontrollers;

import com.hedera.tokendemo.domain.User;
import com.hedera.tokendemo.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@CrossOrigin(maxAge = 3600)
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) throws Exception {
        this.userService = userService;
    }

    @GetMapping(value = "/users", produces = "application/json")
    public List<User> users() {
        return userService.getUsers();
    }

    @GetMapping(value = "/allusers", produces = "application/json")
    public List<User> allusers() {
        return userService.getAllUsers();
    }

    @GetMapping(value = "/users/{userName}", produces = "application/json")
    public User userByName(@PathVariable String userName) {
        try {
            return userService.getFromName(userName);
        } catch (RuntimeException e) {
            return new User();
        }
    }
}
