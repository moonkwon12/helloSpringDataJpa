package kr.ac.hansung.cse.hellospringdatajpa.controller;

import kr.ac.hansung.cse.hellospringdatajpa.entity.Role;
import kr.ac.hansung.cse.hellospringdatajpa.entity.User;
import kr.ac.hansung.cse.hellospringdatajpa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String adminDashboard(Model model) {
        List<User> users = userService.getAllUsers();

        // 사용자 통계 계산 (roles는 Set<Role> 형태)
        long totalUsers = users.size();
        long adminCount = users.stream()
                .filter(user -> user.getRoles() != null &&
                        user.getRoles().contains(Role.ROLE_ADMIN))
                .count();
        long userCount = totalUsers - adminCount;

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("userCount", userCount);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        // 기존 사용자 관리 페이지는 대시보드로 리다이렉트
        return "redirect:/admin";
    }
}