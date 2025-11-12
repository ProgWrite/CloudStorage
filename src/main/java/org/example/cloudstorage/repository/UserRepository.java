package org.example.cloudstorage.repository;

import org.example.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);


    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Long findIdByUsername(@Param("username") String username);

}
