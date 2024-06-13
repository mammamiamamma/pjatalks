package s26901.pjatalks.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.Input.LoginInput;

@Service
public class AuthService {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public boolean authenticate(LoginInput loginInput, HttpServletRequest request, HttpServletResponse response) throws UsernameNotFoundException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginInput.getUsername());
        if (passwordEncoder.matches(loginInput.getPassword(), userDetails.getPassword())) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, loginInput.getPassword(), userDetails.getAuthorities());
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Manually set the session and cookie
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Create and set the session cookie
            Cookie sessionCookie = new Cookie("JSESSIONID", request.getSession().getId());
            sessionCookie.setHttpOnly(true);
            sessionCookie.setMaxAge(86400); // 1 day
            response.addCookie(sessionCookie);

            return true;
        } else {
            throw new UsernameNotFoundException("Invalid username or password");
        }
    }
}
