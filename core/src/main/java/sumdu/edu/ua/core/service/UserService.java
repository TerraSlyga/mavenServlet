package sumdu.edu.ua.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this(userRepository, new BCryptPasswordEncoder());
    }

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username cannot be blank");
        }
        UserEntity user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User with username '" + username + "' was not found"));

        String role = user.getRole();
        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        } else if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .disabled(!user.isEnabled())
                .build();
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public UserEntity findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0: " + id);
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " was not found"));
    }

    public Optional<UserEntity> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        return userRepository.findByUsername(username.trim());
    }

    public Optional<UserEntity> findByConfirmationCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByConfirmationCode(code.trim());
    }

    @Transactional
    public UserEntity register(String username, String rawPassword) {
        return register(username, rawPassword, "ROLE_USER", false);
    }

    @Transactional
    public UserEntity register(String username, String rawPassword, String role, boolean requireConfirmation) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String trimmedUsername = username.trim();
        if (userRepository.findByUsername(trimmedUsername).isPresent()) {
            throw new IllegalArgumentException("User with username '" + trimmedUsername + "' already exists");
        }

        String assignedRole = (role != null && !role.isBlank()) ? role : "ROLE_USER";
        if (!assignedRole.startsWith("ROLE_")) {
            assignedRole = "ROLE_" + assignedRole;
        }

        UserEntity user = new UserEntity();
        user.setUsername(trimmedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword.trim()));
        user.setRole(assignedRole);

        if (requireConfirmation) {
            user.setEnabled(false);
            user.setConfirmationCode(UUID.randomUUID().toString());
        } else {
            user.setEnabled(true);
            user.setConfirmationCode(null);
        }

        return userRepository.save(user);
    }

    @Transactional
    public boolean confirmUser(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        Optional<UserEntity> optionalUser = userRepository.findByConfirmationCode(code.trim());
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            user.setEnabled(true);
            user.setConfirmationCode(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public UserEntity save(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        } else if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole());
        }
        if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0: " + id);
        }
        userRepository.deleteById(id);
    }
}
