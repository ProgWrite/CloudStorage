package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.UserResponseDto;
import org.example.cloudstorage.exception.UserExistsException;
import org.example.cloudstorage.mapper.UserMapper;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== TRYING TO LOAD USER: {} ===", username);

        return userRepository.findByUsername(username)
                .map(UserMapper.INSTANCE::toUserDetails)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
    }

    @Transactional
    public UserResponseDto create(UserRegistrationRequestDto userRegistrationRequestDto) {
        String username = userRegistrationRequestDto.getUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserExistsException("Username already exists");
        }

        User user = UserMapper.INSTANCE.toEntity(userRegistrationRequestDto);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return UserMapper.INSTANCE.toResponseDto(savedUser);
    }

    public Long getId(String username) {
        Long userId = userRepository.findIdByUsername(username);
        if (userId == null) {
            throw new BadCredentialsException("User not found with username: " + username);
        }
        return userId;
    }

}
