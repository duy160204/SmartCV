package com.example.SmartCV.modules.auth.repository ;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartCV.modules.auth.domain.Role;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);

}
