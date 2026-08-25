package com.momentum.config;

import com.momentum.security.CustomOidcUser;
import com.momentum.security.CustomOidcUserService;
import com.momentum.security.ProfileCompletionInterceptor;
import com.momentum.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    @Value("${spring.security.oauth2.client.registration.google.client-id:NOT_SET}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:NOT_SET}")
    private String googleClientSecret;

    @Value("${DB_USER:NOT_SET}")
    private String dbUser;

    @Value("${DB_PASS:NOT_SET}")
    private String dbPass;

    private final ProfileCompletionInterceptor profileCompletionInterceptor;
    private final CustomOidcUserService customOidcUserService;
    private final UserService userService;

    public WebConfiguration(ProfileCompletionInterceptor profileCompletionInterceptor,
                            CustomOidcUserService customOidcUserService,
                            UserService userService) {
        this.profileCompletionInterceptor = profileCompletionInterceptor;
        this.customOidcUserService = customOidcUserService;
        this.userService = userService;
    }

    @PostConstruct
    public void logEnvironmentValues() {
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(matcher -> matcher
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/register", "/login", "/oauth2/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,
                        "/nutrition/products/*", "/nutrition/composites/*", "/nutrition/recipes/*",
                        "/fitness/exercises/*", "/fitness/workouts/*", "/fitness/plans/*")
                .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/dashboard", "/profile",
                        "/complete-profile/**", "/nutrition/**", "/fitness/**",
                        "/api/v1/nutrition/**", "/api/v1/fitness/**").hasRole("USER")
                .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oauth2AuthenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                              Authentication authentication) throws IOException, ServletException {
                String redirectUrl = determineRedirectUrl(authentication);
                response.sendRedirect(redirectUrl);
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                if (authentication.getPrincipal() instanceof CustomOidcUser customOidcUser) {
                    log.debug("OAuth2 login success for Google user: {}", customOidcUser.getUser().getUsername());
                }

                String redirectUrl = determineRedirectUrl(authentication);
                response.sendRedirect(redirectUrl);
            }
        };
    }

    private AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            boolean isDisabled = exception instanceof DisabledException
                    || (exception instanceof OAuth2AuthenticationException oauth2Ex
                    && oauth2Ex.getError() != null
                    && "account_disabled".equals(oauth2Ex.getError().getErrorCode()));
            response.sendRedirect(isDisabled ? "/login?error=disabled" : "/login?error");
        };
    }

    private String determineRedirectUrl(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        return isAdmin ? "/admin/dashboard" : "/dashboard";
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(profileCompletionInterceptor)
                .addPathPatterns("/dashboard");
    }
}
