package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.userDto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.userDto.UserResponseDto;
import org.example.cloudstorage.exception.MinioOperationException;
import org.example.cloudstorage.exception.UserExistsException;
import org.example.cloudstorage.exception.UserRegistrationException;
import org.example.cloudstorage.mapper.UserMapper;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DirectoryService directoryService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== TRYING TO LOAD USER: {} ===", username);

        return userRepository.findByUsername(username)
                .map(userMapper::toUserDetails)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
    }

    @Transactional
    public UserResponseDto createUserWithRootDirectory(UserRegistrationRequestDto userDto) {
        try {
            User user = createUser(userDto);

            log.info("Attempting create root folder for user: {}", user.getUsername());
            directoryService.createRootDirectory(user.getId());
            log.info("Root folder successfully created for user: {}", user.getUsername());
            return userMapper.toResponseDto(user);

        } catch (DataIntegrityViolationException exception) {
            log.warn("User already exists with username {}", userDto.getUsername());
            throw new UserExistsException("Username already exists", exception);

        } catch (MinioOperationException exception) {
            log.warn("Failed to create root directory for user {}", userDto.getUsername(), exception);
            throw new UserRegistrationException("Storage initialization failed", exception);
        }

    }

    public Long getId(String username) {
        Long userId = userRepository.findIdByUsername(username);
        if (userId == null) {
            log.warn("User not found with username {}", username);
            throw new BadCredentialsException("User not found with username: " + username);
        }
        return userId;
    }

    private User createUser(UserRegistrationRequestDto userDto) {
        User user = userMapper.toEntity(userDto);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return savedUser;
    }

}
