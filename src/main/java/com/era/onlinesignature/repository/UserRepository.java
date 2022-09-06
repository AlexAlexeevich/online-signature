package com.era.onlinesignature.repository;

import com.era.onlinesignature.entity.Role;
import com.era.onlinesignature.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByLoginAndSmsCode(String login, Long smsCode);
    Boolean existsByLogin(String login);
}
