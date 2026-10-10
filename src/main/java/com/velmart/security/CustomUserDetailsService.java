package com.velmart.security;

import com.velmart.model.AccountStatus;
import com.velmart.model.User;
import com.velmart.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email address cannot be empty");
        }
        User user = userRepository.findByNormalizedEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new LockedException("Your VELMART account has been suspended. Please contact administrator support.");
        }
        if (user.getAccountStatus() == AccountStatus.INACTIVE) {
            throw new DisabledException("Your VELMART account is inactive.");
        }

        return new UserPrincipal(user);
    }
}
