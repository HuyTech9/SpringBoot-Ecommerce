package com.brian.ecommerce.project.repositories;

import com.brian.ecommerce.project.model.AppRole;
import com.brian.ecommerce.project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(AppRole appRole);
}
