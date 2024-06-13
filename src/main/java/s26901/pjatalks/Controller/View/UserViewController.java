package s26901.pjatalks.Controller.View;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.General.FollowingDto;
import s26901.pjatalks.DTO.General.NotificationDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.FollowingViewDto;
import s26901.pjatalks.DTO.View.UserViewDto;
import s26901.pjatalks.Exception.RecordExistsException;
import s26901.pjatalks.Service.FollowingService;
import s26901.pjatalks.Service.NotificationService;
import s26901.pjatalks.Service.UserService;

import java.time.Instant;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserViewController {
    private final UserService userService;
    private final FollowingService followingService;
    private final NotificationService notificationService;

    public UserViewController(UserService userService, FollowingService followingService, NotificationService notificationService) {
        this.userService = userService;
        this.followingService = followingService;
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    public String getUser(
            @PathVariable @ObjectIdValidation String id, Model model,
                          @RequestParam(defaultValue = "5") int size){
        try {
            UserViewDto userViewDto = userService.getUserForView(id, 0, size);
            model.addAttribute("user", userViewDto);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = ((UserDetails) authentication.getPrincipal()).getUsername();
                if (username != null) {
                    Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
                    int followStatus = 0; // 1 if following, -1 if not
                    if (userOutputDto.isPresent() && !userOutputDto.get().getId().equals(id)) {
                        followStatus = followingService.doesUserFollowUser(userOutputDto.get().getId(), id) ? 1 : -1;
                        model.addAttribute("hasNewNotifications", userService.hasNewNotifications(userOutputDto.get().getId()));
                    }
                    model.addAttribute("followStatus", followStatus);
                }
            }
        } catch (NoSuchElementException e){
            model.addAttribute("errorValid", "No user with such id found!");
            return "redirect:/feed";
        } catch (Exception e){
            model.addAttribute("errorValid", e.getMessage());
            return "redirect:/feed";
        }
        model.addAttribute("size", size);
        model.addAttribute("followRequest", new FollowingDto());
        model.addAttribute("isClickableLink", true);
        return "profile";
    }

    @GetMapping("/{id}/followers")
    public String getUserFollowers(@PathVariable @ObjectIdValidation String id, Model model,
                          @RequestParam(defaultValue = "15") int size){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = ((UserDetails) authentication.getPrincipal()).getUsername();
                if (username != null) {
                    Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
                    if (userOutputDto.isPresent() && !userOutputDto.get().getId().equals(id)) {
                        model.addAttribute("hasNewNotifications", userService.hasNewNotifications(userOutputDto.get().getId()));
                    }
                }
            }
            FollowingViewDto userViewDto = followingService.getFollowingView(id);
            model.addAttribute("followUser", userViewDto);
        } catch (NoSuchElementException e){
            model.addAttribute("errorValid", "No user with such id found!");
        } catch (Exception e){
            model.addAttribute("errorValid", e.getMessage());
        }
        model.addAttribute("size", size);
        return "following";
    }

//    @PostMapping("/follow")
//    public String followUser(@AuthenticationPrincipal UserPrincipal userPrincipal,
//                             @ObjectIdValidation @ModelAttribute("userId") String userId,
//                             RedirectAttributes redirectAttributes,
//                             @RequestHeader(value = "Referer", required = false) String referer){
//        try {
//            if (userPrincipal != null){
//                Optional<UserOutputDto> userOutputDto = userService.findByUsername(userPrincipal.getName());
//                if (userOutputDto.isPresent()){
//                    if (followingService.addFollower(userOutputDto.get().getId(), userId))
//                        redirectAttributes.addFlashAttribute("followSuccess", true);
//                    else redirectAttributes.addFlashAttribute("followSuccess", false);
//                } else throw new IllegalArgumentException("How is this even possible?");
//            }
//        } catch (RecordExistsException e){
//            redirectAttributes.addFlashAttribute
//                    ("error", "You already follow this user");
//        } catch (IllegalArgumentException | ConstraintViolationException e){
//            redirectAttributes.addFlashAttribute
//                    ("error", e.getMessage());
//        }
//        return "redirect:" + (referer != null ? referer : "/feed");
//    }

    @PostMapping("/follow")
    public String followUser(
                             @Valid @ModelAttribute("followRequest") FollowingDto followingDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer", required = false) String referer){
        try {
            if (result.hasErrors()){
                redirectAttributes.addFlashAttribute("errorMap",
                        result.getFieldErrors().stream()
                                .collect(Collectors.toMap(
                                        FieldError::getField,
                                        FieldError::getDefaultMessage
                                )));
                return "redirect:" + (referer != null ? referer : "/feed");
            }
            if (followingService.addFollower(followingDto)!=null){
                redirectAttributes.addFlashAttribute("followSuccess", true);
                sendNotificationToFollowedUser(followingDto.getF_follower_id(), followingDto.getF_user_id());
            } else redirectAttributes.addFlashAttribute("followSuccess", false);
        } catch (RecordExistsException e){
            redirectAttributes.addFlashAttribute
                    ("errorValid", "You already follow this user");
        } catch (IllegalArgumentException | ConstraintViolationException e){
            redirectAttributes.addFlashAttribute
                    ("errorValid", e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/feed");
    }

    @PostMapping("/unfollow")
    public String unfollowUser(
            @Valid @ModelAttribute("followRequest") FollowingDto followingDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer){
        try {
            if (result.hasErrors()){
                redirectAttributes.addFlashAttribute("errorMap",
                        result.getFieldErrors().stream()
                                .collect(Collectors.toMap(
                                        FieldError::getField,
                                        FieldError::getDefaultMessage
                                )));
                return "redirect:" + (referer != null ? referer : "/feed");
            }
            if (followingService.deleteFollowerFromUser(followingDto)){
                redirectAttributes.addFlashAttribute("unfollowSuccess", true);
            } else redirectAttributes.addFlashAttribute("unfollowSuccess", false);
//            redirectAttributes.addFlashAttribute("success", "You have unfollowed this user");
        } catch (Exception e){
            redirectAttributes.addFlashAttribute
                    ("error", e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/feed");
    }

    private void sendNotificationToFollowedUser(String user_id, String target_id){
        if (target_id.equals(user_id)) return; //don't send notifications to yourself
        NotificationDto notificationDto = new NotificationDto(
                "is following you",
                Date.from(Instant.now()),
                target_id,
                user_id,
                "SUB"
        );
        notificationService.addUserNotification(notificationDto);
    }

    @ExceptionHandler({HandlerMethodValidationException.class})
    public String handle(HandlerMethodValidationException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorValid", "Error - User/Post with provided ID not found");
        return "redirect:/feed";
    }
}

