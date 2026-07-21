package org.stock_trading.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.stock_trading.user_service.dto.AuthResponse;
import org.stock_trading.user_service.dto.LoginRequest;
import org.stock_trading.user_service.dto.RegisterRequest;
import org.stock_trading.user_service.entity.User;
import org.stock_trading.user_service.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;


    public AuthResponse register(RegisterRequest request) {

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .balance(request.getBalance())
                .build();

        repository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .message("Registration Successful")
                .build();

    }

    public AuthResponse login(LoginRequest request) {

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .message("Login Successful")
                .build();
    }
}
