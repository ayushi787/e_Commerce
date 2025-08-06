package controller;

import Repository.UserRepository;
import Service.AuthService;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

public class AuthController {
    @Controller
    public class AuthController {
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @GetMapping("/login")
        public String loginPage() {
            return "login";
        }

        @GetMapping("/register")
        public String registerPage() {
            return "register";
        }

        @PostMapping("/doRegister")
        public String doRegister(@ModelAttribute User user) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER"); // default
            userRepository.save(user);
            return "redirect:/login";
        }

        @GetMapping("/default")
        public String defaultAfterLogin(Authentication authentication) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/dashboard";
            }
        }

        @Autowired
        private AuthService authService;
    }
}
