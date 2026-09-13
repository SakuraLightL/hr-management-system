package com.portfolio.hr_system.dto;
import com.portfolio.hr_system.entity.AuthProvider;
import com.portfolio.hr_system.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDto {
    @Positive private Long id;
    @NotBlank @Size(max = 100) private String username;
    @Email @Size(max = 255) private String email;
    @Size(max = 72) private String password;
    @NotNull private Role role = Role.USER;
    @NotNull private AuthProvider provider = AuthProvider.LOCAL;
}
