package com.authorbooksystem.crud.security;

import com.authorbooksystem.crud.entity.User;
import com.authorbooksystem.crud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username){
        User user=userRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("User Not Found"));
        return new UserPrincipal(user);
    }
}
