package com.example.iot_backend.service;

import com.example.iot_backend.dto.LoginUserDto;
import com.example.iot_backend.model.User;
import com.example.iot_backend.dto.RegisterUserDto;
import com.example.iot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    public String signup(RegisterUserDto registerUserDto) {
        User user = new User();
        System.out.println("USER-Email: " + registerUserDto.getEmail());
        System.out.println("USER-Password: " + registerUserDto.getPassword());
        user.setEmail(registerUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    public String authenticate(LoginUserDto loginUserDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword())
        );
        UserDetails user = userRepository.findByEmail(loginUserDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return jwtService.generateToken(user);
    }
}