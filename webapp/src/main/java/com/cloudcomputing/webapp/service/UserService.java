package com.cloudcomputing.webapp.service;

import com.cloudcomputing.webapp.vo.UserDetailsVO;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity createUser(UserDetailsVO userDetails);

    ResponseEntity getUser(Integer userId, String header);

    ResponseEntity updateUser(Integer userId, String header, UserDetailsVO userUpdateDetails);

}
