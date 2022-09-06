package com.era.onlinesignature.security;

import com.era.onlinesignature.entity.User;
import com.era.onlinesignature.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserService userService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login : " + login));
        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userService.findById(id).orElseThrow(() ->
                new UsernameNotFoundException("User not found with id : " + id)
        );
        return UserPrincipal.create(user);
    }
}
