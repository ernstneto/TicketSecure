package com.ticketsecure.repository;

import org.springframework.stereotype.Repository;

import com.ticketsecure.domain.model.User;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    Optional<User> findByCpf(String cpf);
    
    boolean existsByEmail(String email);
}
