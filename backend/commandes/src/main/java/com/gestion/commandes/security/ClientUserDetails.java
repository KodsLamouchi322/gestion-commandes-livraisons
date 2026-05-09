package com.gestion.commandes.security;

import com.gestion.commandes.entity.Client;
import com.gestion.commandes.entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClientUserDetails implements UserDetails {

    private final Client client;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String r = client.getRole() == Role.ADMIN ? "ROLE_ADMIN" : "ROLE_CLIENT";
        return List.of(new SimpleGrantedAuthority(r));
    }

    @Override
    public String getPassword() {
        return client.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return client.getEmail();
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
