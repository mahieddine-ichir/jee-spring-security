package net.michir.config.custom;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by michir on 01/05/2018.
 */
public class MyUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
        try {
            String decoded = new String(Base64.getDecoder().decode(token)).trim();

            String[] split = decoded.split(":");
            String username = split[0];
            String password = split[1];
            if ("user".equals(username) && "user".equals(password)) {
                return new User(username, "changed", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
            } else if ("app".equals(username) && "app".equals(password)) {
                return new User(username, "changed", Collections.singleton(new SimpleGrantedAuthority("ROLE_APPLICATION")));
            } else if ("mixed".equals(username) && "mixed".equals(password)) {
                Collection<GrantedAuthority> auths = new ArrayList<>();
                auths.add(new SimpleGrantedAuthority("ROLE_APPLICATION"));
                auths.add(new SimpleGrantedAuthority("ROLE_USER"));
                return new User(username, "changed", auths);
            } else {
                throw new UsernameNotFoundException("No user found for token "+token);
            }
        } catch (Exception e) {
            throw new UsernameNotFoundException("Authentication failed", e);
        }
    }
}
