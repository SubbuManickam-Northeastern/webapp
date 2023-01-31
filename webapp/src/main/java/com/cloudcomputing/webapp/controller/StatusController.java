package com.cloudcomputing.webapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class StatusController {

    @GetMapping("healthz")
    public ResponseEntity<String> getAppStatus() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
