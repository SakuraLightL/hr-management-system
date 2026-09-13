package com.portfolio.hr_system.config;
import com.portfolio.hr_system.dto.UserDto;
import com.portfolio.hr_system.entity.Role;
import com.portfolio.hr_system.repository.UserRepository;
import com.portfolio.hr_system.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdmin implements ApplicationRunner {
    private final UserRepository repository;
    private final UserService service;
    private final String username;
    private final String password;
    public BootstrapAdmin(UserRepository repository, UserService service,
            @Value("${BOOTSTRAP_ADMIN_USERNAME:}") String username,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        this.repository = repository; this.service = service; this.username = username; this.password = password;
    }
    @Override public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank() || repository.count() != 0) return;
        UserDto dto = new UserDto(); dto.setUsername(username); dto.setPassword(password); dto.setRole(Role.ADMIN);
        service.save(dto);
    }
}
