package com.ticketsecure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsecure.domain.enumerate.Role;
import com.ticketsecure.domain.model.User;

@SpringBootTest
@Transactional
public class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindUser() {
        User user = new User(
            null,
            "Ernst",
            "ernst@teste.com",
            "hashTemporario123",
            "12345678901",
            Role.CLIENT,
            LocalDate.now()
        );
        
        userRepository.save(user);
        //String email = user.getEmail();
        System.out.println("Saved user email: " + user.getEmail());
        Optional<User> userFind = userRepository.findByEmail(user.getEmail());
        
        assertTrue(userFind.isPresent(), "User must be found by email");
        assertEquals("Ernst", userFind.get().getName(), "User name should match");
        assertEquals("12345678901", userFind.get().getCpf(), "User CPF should match");
    }
}