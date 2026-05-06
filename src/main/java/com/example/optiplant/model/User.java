package com.example.optiplant.model;

import com.example.optiplant.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Application user entity and Spring Security principal.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity implements UserDetails {

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = true)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /**
     * Returns authorities derived from the persisted role.
     *
     * @return granted authorities for Spring Security
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the stored password hash used by Spring Security.
     *
     * @return password hash
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Indicates whether the account is non-expired.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is non-locked.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether credentials are non-expired.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is enabled.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}

