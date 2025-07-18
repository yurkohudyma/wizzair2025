package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.domain.User;
import ua.hudyma.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {

    public final UserService userService;

    @PatchMapping("/{userId}")
    public ResponseEntity<User> updateUser (@PathVariable String userId, @RequestBody User user){
        userService.updateUser(userId, user);
        return ResponseEntity.ok().build();
    }
}
