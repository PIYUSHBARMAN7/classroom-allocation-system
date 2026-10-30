package com.classroomallocation.backend.config;

import com.classroomallocation.backend.entity.User;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@Profile("bootstrap-admin")
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            throw new IllegalStateException(
                    "Admin setup stopped: users already exist."
            );
        }

        String email = environment.getRequiredProperty("ADMIN_EMAIL")
                .trim()
                .toLowerCase(Locale.ROOT);

        String password = environment.getRequiredProperty("ADMIN_PASSWORD");

        if (email.length() > 254
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid admin email.");
        }

        if (password.codePointCount(0, password.length()) < 15
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Use at least 15 characters and at most 72 UTF-8 bytes."
            );
        }

        User admin = new User(
                "Administrator",
                email,
                passwordEncoder.encode(password),
                Role.ADMIN
        );

        userRepository.saveAndFlush(admin);

        System.out.println(
                "Admin account created. Stop and restart without the bootstrap profile."
        );
    }
}