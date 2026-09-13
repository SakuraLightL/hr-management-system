package com.portfolio.hr_system.service;
import com.portfolio.hr_system.dto.UserDto;
import com.portfolio.hr_system.entity.*;
import com.portfolio.hr_system.exception.DuplicateResourceException;
import com.portfolio.hr_system.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service @Transactional @RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable pageable) { return repository.findAll(pageable).map(this::toDto); }
    @Transactional(readOnly = true)
    public UserDto findById(Long id) { return toDto(require(id)); }
    private User require(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません"));
    }
    public void save(UserDto dto) {
        User user = dto.getId() == null ? new User() : require(dto.getId());
        String username = dto.getUsername().trim();
        String email = dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail().trim();
        repository.findByUsername(username).filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(other -> { throw new DuplicateResourceException("このユーザー名は登録済みです"); });
        if (email != null) repository.findByEmail(email).filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(other -> { throw new DuplicateResourceException("このメールは登録済みです"); });
        if (dto.getProvider() == AuthProvider.GOOGLE && email == null) {
            throw new IllegalArgumentException("Googleログインにはメールが必要です");
        }
        if (user.getId() != null && user.getProvider() != dto.getProvider()) {
            throw new IllegalArgumentException("既存ユーザーの認証方式は変更できません");
        }
        if (user.getProviderId() != null && !java.util.Objects.equals(user.getEmail(), email)) {
            throw new IllegalArgumentException("連携済みGoogleアカウントのメールは変更できません");
        }
        String password = dto.getPassword();
        if (dto.getProvider() == AuthProvider.LOCAL) {
            if (password != null && !password.isBlank()) {
                // Do not accept a client-supplied encoded credential or accidentally encode one twice.
                if (password.matches("^\\$2[aby]\\$.*")) {
                    if (!password.equals(user.getPassword())) throw new IllegalArgumentException("平文の新しいパスワードを入力してください");
                } else {
                    if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)
                        throw new IllegalArgumentException("パスワードは8文字以上・UTF-8で72バイト以内です");
                    user.setPassword(passwordEncoder.encode(password));
                }
            }
            if (user.getPassword() == null || user.getPassword().isBlank())
                throw new IllegalArgumentException("新規ユーザーにはパスワードが必要です");
        } else { user.setPassword(null); }
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(dto.getRole());
        user.setProvider(dto.getProvider());
        repository.save(user);
    }
    public void delete(Long id) { repository.delete(require(id)); }
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId()); dto.setUsername(user.getUsername()); dto.setEmail(user.getEmail());
        dto.setRole(user.getRole()); dto.setProvider(user.getProvider());
        return dto; // Passwords and provider subjects are never returned to a form.
    }
}
