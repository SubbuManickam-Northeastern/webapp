package com.cloudcomputing.webapp;

import com.cloudcomputing.webapp.controller.StatusController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class StatusControllerTest {

    @Test
    public void getStatusTest() {
        StatusController statusController = new StatusController();
        ResponseEntity status = statusController.getAppStatus();
        assertEquals(new ResponseEntity<>(HttpStatus.OK), status);
    }
}
