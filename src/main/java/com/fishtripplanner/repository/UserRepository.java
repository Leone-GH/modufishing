package com.fishtripplanner.repository;

import com.fishtripplanner.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPassword(String username, String password);
    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);
    boolean existsByUsername(String username); // username 필드 기준

}