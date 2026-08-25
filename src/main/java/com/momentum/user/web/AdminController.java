package com.momentum.user.web;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.service.AdminService;
import com.momentum.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    @Autowired
    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String query,
                            @RequestParam(required = false) UserRole role,
                            @RequestParam(required = false) Boolean enabled,
                            @AuthenticationPrincipal Object principal, Model model) {
        User currentAdmin = userService.getCurrentUser(principal);
        List<User> users = userService.searchUsers(query, role, enabled);
        model.addAttribute("users", users);
        model.addAttribute("query", query);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedEnabled", enabled);
        model.addAttribute("currentUserId", currentAdmin.getId());
        model.addAttribute("activeAdminCount", userService.countActiveAdmins());
        return "admin/users";
    }

    @PostMapping("/users/{userId}/role")
    public String updateUserRole(@PathVariable UUID userId,
                                 @RequestParam UserRole role,
                                 RedirectAttributes redirectAttributes) {
        log.info("Admin updating user role - userId: {}, role: {}", userId, role);
        adminService.updateUserRole(userId, role, redirectAttributes);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/enabled")
    public String toggleUserEnabled(@PathVariable UUID userId,
                                    @RequestParam boolean enabled,
                                    @AuthenticationPrincipal Object principal,
                                    RedirectAttributes redirectAttributes) {
        User currentAdmin = userService.getCurrentUser(principal);
        log.info("Admin setting enabled={} for userId: {}", enabled, userId);
        adminService.toggleUserEnabled(userId, enabled, currentAdmin.getId(), redirectAttributes);
        return "redirect:/admin/users";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("stats", adminService.getStats());
        model.addAttribute("pendingCount", adminService.getModerationQueue().getTotalPending());
        return "admin/dashboard";
    }

    @GetMapping("/moderation")
    public String moderationQueue(Model model) {
        model.addAttribute("queue", adminService.getModerationQueue());
        return "admin/moderation";
    }

    @PostMapping("/moderation/{type}/{id}/approve")
    public String approveItem(@PathVariable String type, @PathVariable UUID id, RedirectAttributes redirectAttributes) {
        adminService.approveItem(type, id, redirectAttributes);
        return "redirect:/admin/moderation";
    }

    @PostMapping("/moderation/{type}/{id}/reject")
    public String rejectItem(@PathVariable String type, @PathVariable UUID id, RedirectAttributes redirectAttributes) {
        adminService.rejectItem(type, id, redirectAttributes);
        return "redirect:/admin/moderation";
    }
}

