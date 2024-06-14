package s26901.pjatalks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import s26901.pjatalks.Service.UserService;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {
    @Autowired
    @Lazy
    private UserService userService;
    @Value("${spring.websecurity.debug:false}")
    boolean webSecurityDebug;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/admin**").hasAnyRole("ADMIN", "OWNER")
                                .requestMatchers(
//                                        "/images/**",
                                        "/img/**",
                                        "/register",
                                        "/api/**",
                                        "/login**",
//                                        "/css/**",
//                                        "/js/**",
                                        "/auth/**",
                                        "/feed"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/auth")
//                                .loginProcessingUrl("/login")
//                                .defaultSuccessUrl("/feed", true)
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/auth?logout=true")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                )
                .rememberMe(rememberMe ->
                        rememberMe
                                .userDetailsService(userService)
                                .tokenValiditySeconds(86400) // Token valid for one day
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                                .maximumSessions(1)
                )
                .csrf(AbstractHttpConfigurer::disable);


        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(webSecurityDebug);
    }
//    @Bean
//    public UserDetailsService userDetailsService(UserService userService) {
//        return userService;

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return userService;
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
