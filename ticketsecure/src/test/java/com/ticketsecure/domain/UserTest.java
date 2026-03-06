package com.ticketsecure.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.ticketsecure.domain.enumerate.Role;
import com.ticketsecure.domain.model.User;

public class UserTest {

    @Test
    void deveCriarUsuarioComSucessoNaMemoria() {
        
        // Arrange & Act usando o padrão BUILDER
        User user = User.builder()
            .name("Ernst")
            .email("ernst@teste.com")
            .senhaHash("senha123")
            .cpf("12345678901")
            .role(Role.CLIENT)
            // Não precisamos passar ID nem Data, a classe gera sozinha!
            .build(); 

        // Assert
        assertNotNull(user.getId(), "O ID deveria ter sido gerado automaticamente");
        assertNotNull(user.getCreatedDate(), "A data deveria ter sido gerada automaticamente");
        assertEquals("Ernst", user.getName());
    }
}