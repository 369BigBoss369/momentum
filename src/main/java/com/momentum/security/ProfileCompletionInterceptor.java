package com.momentum.security;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ProfileCompletionInterceptor implements HandlerInterceptor {
    private final UserService userService;

    @Autowired
    public ProfileCompletionInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return true;
        }

        User user;

        try {
            user = userService.getCurrentUser(auth.getPrincipal());
        } catch (Exception e) {

            return true;
        }

        if (user == null) {

            return true;
        }

        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        if (user.getHeight() == null ||
                user.getWeight() == null ||
                user.getAge() == null ||
                user.getGoal() == null) {
            response.sendRedirect("/complete-profile/step1");
            return false;
        }

        return true;
    }
}

