package com.ticketsecure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsecure.domain.enumerate.Role;
import com.ticketsecure.domain.model.User;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindUser() {
        User user = new User(
            null,
            "Ernst Zeidler",
            "ernst@teste.com",
            "hashTemporario123",
            "12345678901",
            Role.CLIENT,
            LocalDate.now()
        );
        
        Optional<User> existingUser = userRepository.findByCpf(user.getCpf());
        if(existingUser.isPresent()) {
            assertTrue(existingUser.isPresent(), "User must be found by CPF");
            assertEquals("Ernst Zeidler", existingUser.get().getName(), "User name should match");
            assertEquals("12345678901", existingUser.get().getCpf(), "User CPF should match");
            return ;
        }

        userRepository.save(user);
        //String email = user.getEmail();
        System.out.println("Saved user email: " + user.getEmail());
        Optional<User> userFind = userRepository.findByEmail(user.getEmail());
        
        assertTrue(userFind.isPresent(), "User must be found by email");
        assertEquals("Ernst Zeidler", userFind.get().getName(), "User name should match");
        assertEquals("12345678901", userFind.get().getCpf(), "User CPF should match");
    }
}