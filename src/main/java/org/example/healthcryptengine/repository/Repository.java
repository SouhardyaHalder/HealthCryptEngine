package org.example.healthcryptengine.repository;

import org.example.healthcryptengine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Repository extends JpaRepository<User, Long> {
        User findByUsername(String username);
}
