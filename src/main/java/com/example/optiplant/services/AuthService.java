package com.example.optiplant.services;

import com.example.optiplant.dto.AuthResponse;
import com.example.optiplant.dto.LoginRequest;
import com.example.optiplant.dto.RegisterRequest;
import com.example.optiplant.dto.UserResponse;
import com.example.optiplant.exceptions.BadRequestException;
import com.example.optiplant.exceptions.NotFoundException;
import com.example.optiplant.model.Branch;
import com.example.optiplant.model.User;
import com.example.optiplant.model.enums.Role;
import com.example.optiplant.repository.BranchRepository;
import com.example.optiplant.repository.UserRepository;
import com.example.optiplant.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is already registered");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.OPERATOR);

        if (request.branchId() != null) {
            Branch branch = branchRepository.findById(request.branchId())
                    .orElseThrow(() -> new NotFoundException("Branch not found"));
            user.setBranch(branch);
        }

        User savedUser = userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(savedUser), UserResponse.from(savedUser));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String usernameOrEmail = request.usernameOrEmail().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usernameOrEmail, request.password())
        );

        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new NotFoundException("User not found"));

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.from(user);
    }

    public java.util.List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }
}
