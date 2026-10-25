package com.bassim.tp2_securPharmacie.repositories;

import com.bassim.tp2_securPharmacie.models.RolUser;
import com.bassim.tp2_securPharmacie.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUsername(String username);

    @Query("select count(distinct u) from User u join u.roles r where r.rol = :rol")
    long countByRole(@Param("rol") RolUser rol);
}
