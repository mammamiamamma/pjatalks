package s26901.pjatalks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Service.UserService;

import java.util.Optional;

@Component
public class UserInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public UserInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                Optional<UserOutputDto> user = userService.findByUsername(username);
                if (user.isPresent()) {
                    modelAndView.addObject("loggedInUser", user.get());
                }
            }
        }
    }
}
