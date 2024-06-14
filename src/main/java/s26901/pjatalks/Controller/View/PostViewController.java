package s26901.pjatalks.Controller.View;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import s26901.pjatalks.DTO.General.CommentDto;
import s26901.pjatalks.DTO.General.NotificationDto;
import s26901.pjatalks.DTO.Input.PostInputDto;
import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.CommentViewDto;
import s26901.pjatalks.DTO.View.PostViewDto;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Service.*;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/post")
public class PostViewController {
    private final PostService postService;
    private final UserService userService;
    private final LikeService likeService;
    private final NotificationService notificationService;
    private final CommentService commentService;

    public PostViewController(PostService postService, UserService userService, LikeService likeService, NotificationService notificationService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.likeService = likeService;
        this.notificationService = notificationService;
        this.commentService = commentService;
    }

//    private UserOutputDto checkIfNewNotifications(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated()) {
//            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
//            if (username != null) {
//                Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
//                if (userOutputDto.isPresent()){
//                    return userService.hasNewNotifications(userOutputDto.get().getId());
//                }
//            }
//        }
//        return false;
//    }

    @GetMapping
    public String returnToFeed(){
        return "redirect:/feed";
    }

    @GetMapping("/{post_id}")
    public String getPost(@PathVariable @ObjectIdValidation String post_id, Model model, RedirectAttributes redirectAttributes){
        UserOutputDto userOutputDto = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            if (username != null) {
                Optional<UserOutputDto> userOutputDtoOpt = userService.findByUsername(username);
                if (userOutputDtoOpt.isPresent()){
                    userOutputDto = userOutputDtoOpt.get();
                    boolean hasNewNotifications = userService.hasNewNotifications(userOutputDtoOpt.get().getId());
                    model.addAttribute("hasNewNotifications", hasNewNotifications);
                } else {
                    return "redirect:/auth";
                }
            }
        }
//        model.addAttribute("hasNewNotifications", checkIfNewNotifications());

        PostViewDto post = postService.getPostByIdView(post_id, userOutputDto);
        model.addAttribute("post", post);
        if (post != null){
            List<CommentViewDto> commentList = commentService.findAllByPostView(post_id);
            model.addAttribute("commentList", commentList);
        } else {
            redirectAttributes.addFlashAttribute("errorValid", "No post with such id found");
            return "redirect:/feed";
        }
        model.addAttribute("standardDate", new Date());
        model.addAttribute("newComment", new CommentDto());
//        if (principal != null) {
//            String username = principal.getName();
//            // Assuming you have a service to fetch user details by username
//            Optional<UserOutputDto> user = userService.findByUsername(username);
//            user.ifPresent(userOutputDto -> model.addAttribute("user", userOutputDto));
//        }
        return "post";
    }

    @PostMapping("/newPost")
    public String uploadNewPost(@Valid
                                    @ModelAttribute("newPost")
                                    PostInputDto postInputDto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes){
        try {
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMap",
                        result.getFieldErrors().stream().collect(Collectors.toMap(
                                FieldError::getField,
                                FieldError::getDefaultMessage,
                                (existingValue, newValue) -> existingValue+", "+newValue
                        )));
//                redirectAttributes.addFlashAttribute("newPost", postInputDto);
                return "redirect:/feed";
            }
            PostViewDto postOutput = postService.insertPost(postInputDto);
            if (postOutput == null) {
                throw new NotAcknowledgedException("Insert not acknowledged");
            }
//            redirectAttributes.addFlashAttribute("resultPost", postOutput);
            return "redirect:/feed";
        } catch (IllegalArgumentException | NotAcknowledgedException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/feed";
        } catch (ConstraintViolationException e){
            redirectAttributes.addFlashAttribute("errorMap",
                    e.getConstraintViolations()
                            .stream()
                            .collect(Collectors.toMap(
                                    constraintViolation -> constraintViolation.getPropertyPath().toString(),
                                    ConstraintViolation::getMessage
                            )));
            return "redirect:/feed";
        }
    }

    @PostMapping("/newComment")
    public String uploadNewComment(@Valid @ModelAttribute("newComment") CommentDto commentDto, BindingResult result, RedirectAttributes redirectAttributes){
        try {
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMap",
                        result.getFieldErrors().stream().collect(Collectors.toMap(
                                FieldError::getField,
                                FieldError::getDefaultMessage,
                                (existingValue, newValue) -> existingValue+", "+newValue
                        )));
//                redirectAttributes.addFlashAttribute("newPost", postInputDto);
                return "redirect:/post/"+commentDto.getPost_id();
            }
            CommentViewDto commentOutput = commentService.insertCommentToPost(commentDto);
            if (commentOutput == null) {
                throw new NotAcknowledgedException("Insert not acknowledged");
            }
            sendCommentNotificationToPostsAuthor(commentDto.getUser_id(), commentDto.getPost_id());
//            redirectAttributes.addFlashAttribute("resultComment", commentOutput);
            return "redirect:/post/"+commentDto.getPost_id();
        } catch (IllegalArgumentException | NotAcknowledgedException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/post/"+commentDto.getPost_id();
        } catch (ConstraintViolationException e){
            redirectAttributes.addFlashAttribute("errorMap",
                    e.getConstraintViolations()
                            .stream()
                            .collect(Collectors.toMap(
                                    constraintViolation -> constraintViolation.getPropertyPath().toString(),
                                    ConstraintViolation::getMessage
                            )));
            return "redirect:/post/"+commentDto.getPost_id();
        }
    }

    @PostMapping("/like")
    public ResponseEntity<Map<String, Object>> likePost(@RequestParam("postId") String postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
            if (userOutputDto.isPresent()) {
                boolean liked;
                try {
                    liked = likeService.toggleLike(postId, userOutputDto.get().getId(),  Date.from(Instant.now()));
                    if (liked) sendLikeNotificationToPostsAuthor(userOutputDto.get().getId(), postId);
                } catch (ConstraintViolationException e) {
                    return ResponseEntity.badRequest().body(e.getConstraintViolations().stream().collect(Collectors.toMap(
                            violation -> violation.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    )));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().header("reason", e.getMessage()).build();
                }
                long likeCount = likeService.getLikeCountForPost(postId);
                Map<String, Object> response = new HashMap<>();
                response.put("liked", liked);
                response.put("likeCount", likeCount);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private void sendCommentNotificationToPostsAuthor(String user_id, String post_id){
        PostOutputDto postOutputDto = postService.getPostByIdApi(post_id);
        UserOutputDto notificationReciever = userService.getUserById(postOutputDto.getUser_id());
        if (notificationReciever.getId().equals(user_id)) return; //don't send notifications to yourself
        NotificationDto notificationDto = new NotificationDto(
                "commented on your post",
                Date.from(Instant.now()),
                notificationReciever.getId(),
                user_id,
                "COM"
                );
        notificationService.addUserNotification(notificationDto);
    }

    private void sendLikeNotificationToPostsAuthor(String user_id, String post_id){
        PostOutputDto postOutputDto = postService.getPostByIdApi(post_id);
        UserOutputDto notificationReciever = userService.getUserById(postOutputDto.getUser_id());
        if (notificationReciever.getId().equals(user_id)) return; //don't send notifications to yourself
        NotificationDto notificationDto = new NotificationDto(
                "liked your post",
                Date.from(Instant.now()),
                notificationReciever.getId(),
                user_id,
                "LIKE"
        );
        notificationService.addUserNotification(notificationDto);
    }
    @ExceptionHandler({HandlerMethodValidationException.class})
    public String handle(HandlerMethodValidationException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorValid", "Error - User/Post with provided ID not found");
        return "redirect:/feed";
    }
}
