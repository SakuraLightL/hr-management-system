package com.portfolio.hr_system.service;
import com.portfolio.hr_system.dto.UserDto;
import com.portfolio.hr_system.entity.*;
import com.portfolio.hr_system.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    UserRepository repository = mock(UserRepository.class);
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    UserService service = new UserService(repository, encoder);
    User existing;
    UserDto dto;
    @BeforeEach void setup() {
        existing = new User(); existing.setId(1L); existing.setUsername("alice");
        existing.setPassword(encoder.encode("old-password"));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("alice")).thenReturn(Optional.of(existing));
        dto = new UserDto(); dto.setId(1L); dto.setUsername("alice");
    }
    @Test void blankAndNullPasswordPreserveExistingHash() {
        String hash = existing.getPassword();
        for (String value : new String[]{null, "", "   ", hash}) {
            dto.setPassword(value); service.save(dto); assertThat(existing.getPassword()).isEqualTo(hash);
        }
    }
    @Test void changedPasswordIsEncodedOnce() {
        dto.setPassword("new-password"); service.save(dto);
        assertThat(encoder.matches("new-password", existing.getPassword())).isTrue();
    }
    @Test void formNeverContainsExistingHash() { assertThat(service.findById(1L).getPassword()).isNull(); }
    @Test void newLocalUserNeedsPassword() {
        dto.setId(null); dto.setUsername("new-user");
        assertThatThrownBy(() -> service.save(dto)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void arbitraryPreEncodedPasswordIsRejected() {
        dto.setPassword(encoder.encode("attacker-password"));
        assertThatThrownBy(() -> service.save(dto)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void googleProvisioningRequiresEmail() {
        dto.setId(null); dto.setUsername("google-user"); dto.setProvider(AuthProvider.GOOGLE);
        assertThatThrownBy(() -> service.save(dto)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void duplicateUsernameIsRejected() {
        dto.setId(null);
        assertThatThrownBy(() -> service.save(dto)).isInstanceOf(com.portfolio.hr_system.exception.DuplicateResourceException.class);
    }
}
