package net.michir.config.custom;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by michir on 01/05/2018.
 */
public class MyAuthentication implements Authentication {

    private Collection<? extends GrantedAuthority> authorities;

    private String token;

    private UserDetails userDetails;

    private boolean authenticated;

    private String username;

    public MyAuthentication() {
    }

    /**
     * @param userDetails
     * @param username
     * @param token
     * @param authorities can be null
     * @param authenticated
     */
    public MyAuthentication(UserDetails userDetails, String username, Collection<? extends GrantedAuthority> authorities, boolean authenticated) {
        this.authorities = authorities;
        this.userDetails = userDetails;
        this.authenticated = authenticated;
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return userDetails;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return username;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
