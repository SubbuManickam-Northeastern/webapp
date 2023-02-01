package com.cloudcomputing.webapp.serviceimpl;

import com.cloudcomputing.webapp.dto.UserDataDTO;
import com.cloudcomputing.webapp.entity.User;
import com.cloudcomputing.webapp.repository.UserRepository;
import com.cloudcomputing.webapp.service.UserService;
import com.cloudcomputing.webapp.vo.UserDetailsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public ResponseEntity createUser(UserDetailsVO userDetails) {

        UserDataDTO newUser = new UserDataDTO();
        User user = new User();

        try {
            List<String> existingUsers = userRepository.getUsernames();
            String currentDate = String.valueOf(java.time.LocalDateTime.now());

            BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(10);

            Pattern emailCheck = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]+$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = emailCheck.matcher(userDetails.getUsername());

            if(existingUsers.contains(userDetails.getUsername())) {
                return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
            }

            if(!matcher.find()) {
                return new ResponseEntity<>("Please enter a valid email address", HttpStatus.BAD_REQUEST);
            }

            String encryptedPassword = bCrypt.encode(userDetails.getPassword());

            user.setFirstName(userDetails.getFirstName());
            user.setLastName(userDetails.getLastName());
            user.setUsername(userDetails.getUsername());
            user.setPassword(encryptedPassword);
            user.setAccountCreated(currentDate);
            user.setAccountUpdated(currentDate);
            userRepository.save(user);

            User createdUser = userRepository.getByUsername(userDetails.getUsername());
            newUser.setId(createdUser.getId());
            newUser.setFirstName(createdUser.getFirstName());
            newUser.setLastName(createdUser.getLastName());
            newUser.setUsername(createdUser.getUsername());
            newUser.setAccountCreated(createdUser.getAccountCreated());
            newUser.setAccountUpdated(createdUser.getAccountUpdated());

            return new ResponseEntity<>(newUser, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>("Bad Request",HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity getUser(Integer userId, String header) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        UserDataDTO userData = new UserDataDTO();

        try {

            String[] values = authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(!encoder.matches(values[1], authUser.getPassword())) {
                return new ResponseEntity<>("Unauthorized Access. Enter valid credentials", HttpStatus.UNAUTHORIZED);
            }

            User loggedUser = userRepository.getByUserId(userId);
            if(!loggedUser.getUsername().equals(values[0])) {
                return new ResponseEntity<>("Forbidden Access. Cannot access other users", HttpStatus.FORBIDDEN);
            }

            userData.setId(loggedUser.getId());
            userData.setUsername(loggedUser.getUsername());
            userData.setFirstName(loggedUser.getFirstName());
            userData.setLastName(loggedUser.getLastName());
            userData.setAccountCreated(loggedUser.getAccountCreated());
            userData.setAccountUpdated(loggedUser.getAccountUpdated());

            return new ResponseEntity<>(userData, HttpStatus.OK);

        } catch(Exception e) {
            return new ResponseEntity<>("Forbidden Access", HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<String> updateUser(Integer userId, String header, UserDetailsVO userUpdateDetails) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        try {

            String[] values = authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(!encoder.matches(values[1], authUser.getPassword())) {
                return new ResponseEntity<>("Unauthorized Access. Enter valid credentials", HttpStatus.UNAUTHORIZED);
            }

            User loggedUser = userRepository.getByUserId(userId);
            if(!loggedUser.getUsername().equals(values[0])) {
                return new ResponseEntity<>("Forbidden Access", HttpStatus.FORBIDDEN);
            }

            if(!loggedUser.getUsername().equals(userUpdateDetails.getUsername())) {
                return new ResponseEntity<>("Bad Request. Username cannot be updated", HttpStatus.BAD_REQUEST);
            }

            String encryptedPassword = encoder.encode(userUpdateDetails.getPassword());
            String updateDate = String.valueOf(java.time.LocalDateTime.now());

            loggedUser.setFirstName(userUpdateDetails.getFirstName());
            loggedUser.setLastName(userUpdateDetails.getLastName());
            loggedUser.setPassword(encryptedPassword);
            loggedUser.setAccountUpdated(updateDate);
            userRepository.save(loggedUser);

            return new ResponseEntity<>("Update Success", HttpStatus.NO_CONTENT);

        } catch(Exception e) {
            return new ResponseEntity<>("Unauthorized Access", HttpStatus.UNAUTHORIZED);
        }
    }

    public String[] authenticateUser(String authString) {

        if (authString != null && authString.toLowerCase().startsWith("basic")) {
            String cred = authString.substring("basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(cred);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            return values;
        }
        return null;
    }

}
