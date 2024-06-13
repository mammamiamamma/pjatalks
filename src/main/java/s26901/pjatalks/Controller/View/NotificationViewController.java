package s26901.pjatalks.Controller.View;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.NotificationViewDto;
import s26901.pjatalks.Service.NotificationService;
import s26901.pjatalks.Service.UserService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/notifications")
public class NotificationViewController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationViewController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public String getNotifications(@PathVariable @ObjectIdValidation String id,
                                   @RequestHeader(value = "Referer", required = false) String referer,
                                   Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            if (username != null) {
                Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
                if (userOutputDto.isPresent() && !userOutputDto.get().getId().equals(id)) {
                    return "redirect:" + (referer != null ? referer : "/feed");
                }
            }
        }
        try {
            List<NotificationViewDto> notifications = notificationService.getNotificationsForView(id);
            model.addAttribute("notifications", notifications);
            userService.updateLastCheckedNotifications(id);
        } catch (IllegalArgumentException e){
            model.addAttribute("error", e.getMessage());
        }
        return "notifications";
    }
}
