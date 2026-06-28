package com.dbp.democarpultec.repository;

import com.dbp.democarpultec.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByStudentCode(String studentCode);
}
