package com.portfolio.hr_system.repository;

import com.portfolio.hr_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    /**
     * username検索
     */
    Optional<User> findByUsername(String username);

    /**
     * email検索
     */
    Optional<User> findByEmail(String email);

    /**
     * providerId検索
     */
    Optional<User> findByProviderId(String providerId);

    /**
     * role検索
     */
    List<User> findByRole(String role);

    /**
     * username部分一致検索
     */
    List<User> findByUsernameContaining(String username);
}