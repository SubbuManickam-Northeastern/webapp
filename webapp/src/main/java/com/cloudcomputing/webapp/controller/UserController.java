package com.cloudcomputing.webapp.controller;

import com.cloudcomputing.webapp.service.UserService;
import com.cloudcomputing.webapp.vo.UserDetailsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity createUser(@RequestBody UserDetailsVO userDetails) {
        ResponseEntity userData = userService.createUser(userDetails);
        return userData;
    }

    @GetMapping("/{userId}")
    public ResponseEntity getUser(@PathVariable("userId") Integer userId, @RequestHeader(value = "Authorization", required = false) String header) {
        ResponseEntity userData = userService.getUser(userId, header);
        return userData;
    }

    @PutMapping("/{userId}")
    public ResponseEntity updateUser(@PathVariable("userId") Integer userId, @RequestHeader(value = "Authorization", required = false) String header,
                                     @RequestBody UserDetailsVO userUpdateDetails) {
        ResponseEntity updateStatus = userService.updateUser(userId, header, userUpdateDetails);
        return updateStatus;
    }
}
