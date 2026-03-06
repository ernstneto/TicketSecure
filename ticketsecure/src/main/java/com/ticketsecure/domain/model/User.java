package com.ticketsecure.domain.model;

import java.util.UUID;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder; // <-- Nova importação
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import com.ticketsecure.domain.enumerate.Role;

@Data
@Builder // <-- Nova anotação mágica
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Builder.Default // <-- Avisa o Builder para usar esse valor padrão
    private UUID id = UUID.randomUUID(); // <-- Gera sozinho!

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senhaHash;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_date", updatable = false)
    @Builder.Default // <-- Avisa o Builder para usar esse valor padrão
    private LocalDate createdDate = LocalDate.now(); // <-- Pega a data de hoje sozinho!
}