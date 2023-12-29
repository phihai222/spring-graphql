package com.phihai91.springgraphql.securities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;

@Getter
@Setter
@Builder
@With
public class AppUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String username;

    private String email;

    private String password;

    public AppUserDetails(String id, String username, String email, String password, Boolean twoMFA, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.twoMFA = twoMFA;
        this.authorities = authorities;
    }

    private Boolean twoMFA;

    private Collection<? extends GrantedAuthority> authorities;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
