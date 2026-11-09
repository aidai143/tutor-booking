package kg.alatoo.tutorbooking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Login for the administrator.
 * Every page is closed except the login page, the stylesheet and the error page.
 * Spring Security checks the password, keeps the login in a session cookie
 * and adds a CSRF token to every form (th:action does it automatically).
 */
@Configuration
public class SecurityConfig {

    private static final int MIN_PASSWORD_LENGTH = 8;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/style.css", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/lessons", true))
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    /** BCrypt stores only a salted hash: even we cannot read the password back. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** The only user: the administrator. Login and password come from the .env file, not from the code. */
    @Bean
    public UserDetailsService administrator(@Value("${ADMIN_USERNAME:}") String username,
                                            @Value("${ADMIN_PASSWORD:}") String password,
                                            PasswordEncoder encoder) {
        // Fail at startup with a clear message instead of running a site nobody can log into
        if (username.isBlank()) {
            throw new IllegalStateException("ADMIN_USERNAME is not set: add it to the .env file");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least " + MIN_PASSWORD_LENGTH + " characters: check the .env file");
        }
        UserDetails admin = User.withUsername(username)
                .password(encoder.encode(password))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
