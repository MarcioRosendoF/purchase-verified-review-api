package com.marcio.marketplace.service;

import com.marcio.marketplace.dto.request.LoginRequest;
import com.marcio.marketplace.dto.request.RegisterRequest;
import com.marcio.marketplace.dto.response.AuthResponse;
import com.marcio.marketplace.entity.User;
import com.marcio.marketplace.entity.enums.Role;
import com.marcio.marketplace.exception.BusinessException;
import com.marcio.marketplace.repository.UserRepository;
import com.marcio.marketplace.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Tentativa de registro falhou. Email já cadastrado: {}", request.getEmail());
            throw new BusinessException("Email já cadastrado");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        log.info("Usuário registrado com sucesso. ID: {}, Email: {}", user.getId(), user.getEmail());

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            log.warn("Tentativa de login falhou. Credenciais inválidas para o email: {}", request.getEmail());
            throw new BusinessException("Credenciais inválidas");
        }

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        log.info("Usuário autenticado com sucesso. ID: {}, Email: {}", user.getId(), user.getEmail());

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }
}
