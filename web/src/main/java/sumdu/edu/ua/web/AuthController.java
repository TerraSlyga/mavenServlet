package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sumdu.edu.ua.core.service.UserService;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.service.MailService;

@Controller
public class AuthController {

    private final UserService userService;
    private final MailService mailService;

    @Autowired
    public AuthController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error,
                            @RequestParam(name = "logout", required = false) String logout,
                            @RequestParam(name = "registered", required = false) String registered,
                            @RequestParam(name = "confirmed", required = false) String confirmed,
                            @RequestParam(name = "invalidCode", required = false) String invalidCode,
                            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/books";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Невірний логін або пароль");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Ви успішно вийшли із системи");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Реєстрація успішна! На вашу пошту надіслано посилання для підтвердження.");
        }
        if (confirmed != null) {
            model.addAttribute("successMessage", "Акаунт успішно активовано! Тепер ви можете увійти.");
        }
        if (invalidCode != null) {
            model.addAttribute("errorMessage", "Недійсний або прострочений код підтвердження.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/books";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "email", required = false) String email,
            Model model) {

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Логін обов'язковий");
            return "auth/register";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Пароль обов'язковий");
            return "auth/register";
        }

        if (userService.findByUsername(username.trim()).isPresent()) {
            model.addAttribute("errorMessage", "Користувач з таким логіном вже існує");
            return "auth/register";
        }

        try {
            UserEntity created = userService.register(username.trim(), password.trim(), "ROLE_USER", true);
            mailService.sendConfirmationEmail(email, created.getUsername(), created.getConfirmationCode());
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Помилка при реєстрації: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/confirm")
    public String confirmAccount(@RequestParam(name = "code", required = false) String code) {
        if (code == null || code.trim().isEmpty()) {
            return "redirect:/login?invalidCode=true";
        }
        boolean confirmed = userService.confirmUser(code.trim());
        if (confirmed) {
            return "redirect:/login?confirmed=true";
        } else {
            return "redirect:/login?invalidCode=true";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }

    @GetMapping("/404")
    public String notFound() {
        return "error/404";
    }
}
