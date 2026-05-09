package com.ticketsecure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketsecure.domain.enumerate.Role;
import com.ticketsecure.domain.model.User;
import com.ticketsecure.dto.UserRequestDTO;
import com.ticketsecure.dto.UserResponseDTO;
import com.ticketsecure.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        // Aqui você pode adicionar lógica de validação, hashing de senha, etc.
        // Por exemplo, você pode verificar se o email já existe no banco de dados
        if (userRepository.existsByEmail(userRequestDTO.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if(userRepository.findByCpf(userRequestDTO.cpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        // Hash da senha (exemplo simples, use uma biblioteca de hashing real em produção)
        //String senhaHash = Integer.toString(userRequestDTO.senha().hashCode());

        // Criar um novo usuário e salvar no banco de dados
        User user = User.builder()
                .name(userRequestDTO.name())
                .email(userRequestDTO.email())
                .senhaHash(userRequestDTO.senha())
                .cpf(userRequestDTO.cpf())
                .role(Role.CLIENT) // Definindo o papel como CLIENT por padrão
                .build();

        //userRepository.save(user);
        User savedUser = userRepository.save(user);
        return new UserResponseDTO(
            savedUser.getId(), 
            savedUser.getName(), 
            savedUser.getEmail(), 
            savedUser.getCpf(), 
            savedUser.getRole()
        );
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getCpf(), user.getRole());
    }
}
