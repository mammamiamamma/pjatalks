package s26901.pjatalks.Controller.View;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Service.AdminService;
import s26901.pjatalks.Service.PostService;
import s26901.pjatalks.Service.UserService;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/banUser")
    public String banUser(@RequestParam String userId,
                          @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            userService.deleteUser(userId);
        } catch (NotAcknowledgedException e) {
            throw new RuntimeException(e);
        }
        return "redirect:" + (referer != null ? referer : "/admin/dashboard");
    }
}
