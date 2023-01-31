package com.cloudcomputing.webapp.repository;

import com.cloudcomputing.webapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u.username from User u")
    List<String> getUsernames();

    @Query("select u from User u where u.username = :username")
    User getByUsername(@Param("username") String username);

    @Query("select u from User u where u.id = :userId")
    User getByUserId(@Param("userId") Integer userId);
}
