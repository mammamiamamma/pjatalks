package s26901.pjatalks.Controller.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import s26901.pjatalks.DTO.Input.LoginInput;
import s26901.pjatalks.DTO.Input.UserInputDto;
import s26901.pjatalks.Service.AuthService;
import s26901.pjatalks.Service.UserService;

import java.util.stream.Collectors;

@Controller
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/auth")
    public String showLoginForm(Model model) {
        if (isAuthenticated()) {
            return "redirect:/feed";
        }
        model.addAttribute("loginInput", new LoginInput());
        model.addAttribute("userInputDto", new UserInputDto());
        return "login-form";
    }

    @PostMapping("/login")
    public String loginUser(@Valid @ModelAttribute("loginInput") LoginInput loginInput,
                            BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMapLogin",
                        bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                                FieldError::getField,
                                FieldError::getDefaultMessage,
                                (existingValue, newValue) -> existingValue+", "+newValue
                        )));
                redirectAttributes.addFlashAttribute("activeTab", "login");
                return "redirect:/auth";
            }
            boolean authenticated = authService.authenticate(loginInput, request, response);
            if (authenticated) {
                return "redirect:/feed";
            } else {
                redirectAttributes.addFlashAttribute("errorLogin", "Invalid username or password");
                return "redirect:/auth";
            }
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorLogin", "Invalid username or password");
            return "redirect:/auth";
        }
    }

//    @GetMapping("/register")
//    public String showRegistrationForm(Model model) {
//        model.addAttribute("userInputDto", new UserInputDto());
//        return "registration";
//    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userInputDto") UserInputDto userInputDto,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMapRegister",
                        bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                                FieldError::getField,
                                FieldError::getDefaultMessage,
                                (existingValue, newValue) -> existingValue+", "+newValue
                        )));
                redirectAttributes.addFlashAttribute("activeTab", "register");
                return "redirect:/auth";
            }
            userService.registerUser(userInputDto);
            redirectAttributes.addFlashAttribute("successRegister", "Successful registration!");
            return "redirect:/auth";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorRegister", "Registration failed");
            return "redirect:/auth";
        }
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"));
    }
}
