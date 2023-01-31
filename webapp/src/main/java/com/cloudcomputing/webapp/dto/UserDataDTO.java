package com.cloudcomputing.webapp.dto;

import lombok.Data;

@Data
public class UserDataDTO {

    Integer id;
    String firstName;
    String lastName;
    String username;
    String accountCreated;
    String accountUpdated;
}
