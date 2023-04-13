package com.cloudcomputing.webapp.controller;

import com.cloudcomputing.webapp.serviceimpl.UserServiceImpl;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class StatusController {

    @Autowired
    StatsDClient statsDClient;

    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    @GetMapping("healthz")
    public ResponseEntity getAppStatus() {
        if(statsDClient != null) {
            statsDClient.incrementCounter("endpoint.healthz.get");
        }
        logger.info("Success in api /healthz");
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("cicd")
    public ResponseEntity getReviewStatus() {
        logger.info("Success in api /review");
        return new ResponseEntity(HttpStatus.OK);
    }
}
