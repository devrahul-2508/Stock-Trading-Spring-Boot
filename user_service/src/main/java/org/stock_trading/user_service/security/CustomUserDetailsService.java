package org.stock_trading.user_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.stock_trading.user_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return repository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("Email not found"));

    }
}
