package com.bassim.tp2_securPharmacie.repositories;

import com.bassim.tp2_securPharmacie.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {


    Optional<User> findUserByUsername(String username);
}
