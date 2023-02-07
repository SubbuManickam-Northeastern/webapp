package com.cloudcomputing.webapp;

import org.junit.jupiter.api.Test;
import com.cloudcomputing.webapp.controller.StatusController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.Assert.assertEquals;

class WebappApplicationTests {

// 	@Test
// 	void contextLoads() {
// 	}
	
	@Test
	public void getStatusTest() {
		StatusController statusController = new StatusController();
		ResponseEntity status = statusController.getAppStatus();
		assertEquals(new ResponseEntity<>(HttpStatus.OK), new ResponseEntity<>(HttpStatus.FORBIDDEN));
	}
}
