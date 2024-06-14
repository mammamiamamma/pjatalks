package s26901.pjatalks.Controller.View;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Service.AdminService;
import s26901.pjatalks.Service.PostService;
import s26901.pjatalks.Service.UserService;

import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final PostService postService;
    private final AdminService adminService;

    public AdminController(UserService userService, PostService postService, AdminService adminService) {
        this.userService = userService;
        this.postService = postService;
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String getStatistics(Model model) {
        model.addAttribute("totalUsers", adminService.getTotalUsers());
        model.addAttribute("activeUsers", adminService.getActiveUsers());
        model.addAttribute("totalPosts", adminService.getTotalPosts());
        model.addAttribute("totalComments", adminService.getTotalComments());

        // Fetching top users by posts
        Map<UserOutputDto, Integer> topUsersByPosts = adminService.getTopUsersByPosts();
        model.addAttribute("topUsersByPostsMap", topUsersByPosts);

        // Fetching top users by likes (assuming you have this method implemented)
        Map<UserOutputDto, Integer> topUsersByLikes = adminService.getTopUsersByLikes();
        model.addAttribute("topUsersByLikesMap", topUsersByLikes);
//        model.addAttribute("userCount", userService.getUserCount());
//        model.addAttribute("postCount", postService.getPostCount());
        return "analytics";
    }

    @PostMapping("/deletePost")
    public String deletePost(@RequestParam String postId,
                             @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            postService.deletePost(postId);
        } catch (NotAcknowledgedException e) {
            throw new RuntimeException(e);
        }
        return "redirect:" + (referer != null ? referer : "/admin/dashboard");
    }

    @PostMapping("/assignRole")
    public String giveRoleToUser(@RequestParam @ObjectIdValidation String user_id,
                                 @RequestParam String role_name, RedirectAttributes redirectAttributes){
        try {
            userService.addRoleToUser(user_id, role_name);
        } catch (NoSuchElementException e){
            redirectAttributes.addFlashAttribute("error", "Error processing request: no user with such id found!");
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/"+user_id;
    }

    @PostMapping("/deleteRole")
    public String deleteRoleFromUser(@RequestParam @ObjectIdValidation String user_id,
                                 @RequestParam String role_name, RedirectAttributes redirectAttributes){
        try {
            userService.deleteRoleFromUser(user_id, role_name);
        } catch (NoSuchElementException e){
            redirectAttributes.addFlashAttribute("error", "Error processing request: no user with such id found!");
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/"+user_id;
    }

    @PostMapping("/banUser")
    public String banUser(@RequestParam String user_id,
                          @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            postService.deletePostsByUserId(user_id);
            userService.deleteUser(user_id);
        } catch (NotAcknowledgedException e) {
            throw new RuntimeException(e);
        }
        return "redirect:" + (referer != null ? referer : "/admin/dashboard");
    }
}
