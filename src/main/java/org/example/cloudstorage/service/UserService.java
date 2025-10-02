package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.UserRegistrationRequestDto;
import org.example.cloudstorage.dto.UserResponseDto;
import org.example.cloudstorage.exception.UserExistsException;
import org.example.cloudstorage.mapper.UserMapper;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

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

        //TODO по хорошему надо дто здесь!
        return userRepository.findByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.emptyList()
                ))
                .orElseThrow(() -> {
                    log.error("USER NOT FOUND: {}", username);
                    return new UsernameNotFoundException("Failed to load user: " + username);
                });
    }

    @Transactional
    public UserResponseDto create(UserRegistrationRequestDto userRegistrationRequestDto){
        String username = userRegistrationRequestDto.getUsername();
        if(userRepository.findByUsername(username).isPresent()){
            throw new UserExistsException("Username already exists with username: " + username);
        }

        User user = UserMapper.INSTANCE.toEntity(userRegistrationRequestDto);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return UserMapper.INSTANCE.toResponseDto(savedUser);
    }

}
