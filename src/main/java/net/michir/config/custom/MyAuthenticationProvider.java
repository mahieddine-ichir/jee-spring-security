package net.michir.config.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created by michir on 01/05/2018.
 */
public class MyAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        MyAuthentication myAuthentication = (MyAuthentication) authentication;
        UserDetails userDetails = userDetailsService.loadUserByUsername(myAuthentication.getToken());

        return new MyAuthentication(userDetails, userDetails.getUsername(), userDetails.getAuthorities(), true);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MyAuthentication.class.isAssignableFrom(authentication);
    }
}
