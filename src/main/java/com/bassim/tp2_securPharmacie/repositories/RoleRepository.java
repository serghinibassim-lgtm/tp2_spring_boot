package com.bassim.tp2_securPharmacie.repositories;

import com.bassim.tp2_securPharmacie.models.RolUser;
import com.bassim.tp2_securPharmacie.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByRol(RolUser rol);
}
