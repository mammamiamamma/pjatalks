package s26901.pjatalks.Controller.View;

import jakarta.validation.ConstraintViolation;
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
    private final String REDIRECT_FEED = "redirect:/feed";
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

            handleAuthenticatedUserForProfile(model, id);
        } catch (NoSuchElementException e){
            model.addAttribute("errorValid", "No user with such id found!");
            return REDIRECT_FEED;
        } catch (Exception e){
            model.addAttribute("errorValid", e.getMessage());
            return REDIRECT_FEED;
        }

        model.addAttribute("size", size);
        model.addAttribute("followRequest", new FollowingDto());
        model.addAttribute("isClickableLink", true);
        return "profile";
    }

    @GetMapping("/{id}/followers")
    public String getUserFollowers(@PathVariable @ObjectIdValidation String id, Model model,
                          @RequestParam(defaultValue = "15") int size){
        return getUserFollowView(id, model, size, "followers");
    }

    @GetMapping("/{id}/following")
    public String getUserFollowing(@PathVariable @ObjectIdValidation String id, Model model,
                                   @RequestParam(defaultValue = "15") int size){
        return getUserFollowView(id, model, size, "following");
    }

    private String getUserFollowView(String id, Model model, int size, String activeTab) {
        try {
            handleAuthenticatedUserForNotifications(model, id);
        } catch (NoSuchElementException e) {
            model.addAttribute("errorValid", "No user with such id found!");
        } catch (Exception e) {
            model.addAttribute("errorValid", e.getMessage());
        }

        UserOutputDto userOutputDto = userService.getUserById(id);
        if (userOutputDto != null) {
            FollowingViewDto userViewDto = followingService.getFollowingView(userOutputDto);
            model.addAttribute("followUser", userViewDto);
            model.addAttribute("activeTab", activeTab);
        }
        model.addAttribute("size", size);
        return "following";
    }

    private void handleAuthenticatedUserForProfile(Model model, String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
            int followStatus = 0;
            if (userOutputDto.isPresent() && !userOutputDto.get().getId().equals(id)) {
                followStatus = followingService.doesUserFollowUser(userOutputDto.get().getId(), id) ? 1 : -1;
                model.addAttribute("hasNewNotifications", userService.hasNewNotifications(userOutputDto.get().getId()));
            }
            model.addAttribute("followStatus", followStatus);
        }
    }

    private void handleAuthenticatedUserForNotifications(Model model, String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
            if (userOutputDto.isPresent() && !userOutputDto.get().getId().equals(id)) {
                model.addAttribute("hasNewNotifications", userService.hasNewNotifications(userOutputDto.get().getId()));
            }
        }
    }

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
                return referer != null ? ("redirect:" + referer) : REDIRECT_FEED;
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
        return referer != null ? ("redirect:" + referer) : REDIRECT_FEED;
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
                return referer != null ? ("redirect:" + referer) : REDIRECT_FEED;
            }
            if (followingService.deleteFollowerFromUser(followingDto)){
                redirectAttributes.addFlashAttribute("unfollowSuccess", true);
            } else redirectAttributes.addFlashAttribute("unfollowSuccess", false);
        } catch (Exception e){
            redirectAttributes.addFlashAttribute
                    ("error", e.getMessage());
        }
        return referer != null ? ("redirect:" + referer) : REDIRECT_FEED;
    }

    @PostMapping("/updateBio")
    public String updateBio(@RequestParam("userId") @ObjectIdValidation String userId,
                            @RequestParam("shortBio") String shortBio,
                            RedirectAttributes redirectAttributes) {
        try {
            if (shortBio.isEmpty()) throw new IllegalArgumentException("Updated short bio can't be empty");
            userService.updateShortBio(userId, shortBio.trim());
        } catch (NoSuchElementException e){
            redirectAttributes.addFlashAttribute("error", "Error processing request: user with such id not found");
        } catch (ConstraintViolationException e){
            redirectAttributes.addFlashAttribute("errorMap", e.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            violation -> violation.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    )));
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/" + userId;
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
    public String handle(RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorValid", "Error - User/Post with provided ID not found");
        return REDIRECT_FEED;
    }
}

