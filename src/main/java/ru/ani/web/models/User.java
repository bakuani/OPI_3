package ru.ani.web.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * Application user entity implementing Spring Security’s UserDetails.
 * Stores username, password, and role information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "registered_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique username for login. */
    @Column(unique = true, nullable = false)
    private String username;

    /** Encrypted password. */
    @Column(nullable = false)
    private String password;

    /** Role assigned to this user (e.g. USER). */
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return (role == null) ? List.of() : List.of(role);
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
