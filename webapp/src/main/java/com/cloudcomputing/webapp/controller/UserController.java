package com.cloudcomputing.webapp.controller;

import com.cloudcomputing.webapp.service.UserService;
import com.cloudcomputing.webapp.vo.UserDetailsVO;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    StatsDClient statsDClient;

    @PostMapping
    public ResponseEntity createUser(@RequestBody UserDetailsVO userDetails) {
        statsDClient.incrementCounter("endpoint.createUser.post");
        ResponseEntity userData = userService.createUser(userDetails);
        return userData;
    }

    @GetMapping("/{userId}")
    public ResponseEntity getUser(@PathVariable("userId") Integer userId, @RequestHeader(value = "Authorization", required = false) String header) {
        statsDClient.incrementCounter("endpoint.getUser.get");
        ResponseEntity userData = userService.getUser(userId, header);
        return userData;
    }

    @PutMapping("/{userId}")
    public ResponseEntity updateUser(@PathVariable("userId") Integer userId, @RequestHeader(value = "Authorization", required = false) String header,
                                     @RequestBody UserDetailsVO userUpdateDetails) {
        statsDClient.incrementCounter("endpoint.updateUser.put");
        ResponseEntity updateStatus = userService.updateUser(userId, header, userUpdateDetails);
        return updateStatus;
    }
}
