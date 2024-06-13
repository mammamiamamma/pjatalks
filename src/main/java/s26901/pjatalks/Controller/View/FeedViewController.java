package s26901.pjatalks.Controller.View;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import s26901.pjatalks.DTO.Input.PostInputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.PostViewDto;
import s26901.pjatalks.Service.UserService;
import s26901.pjatalks.SupportEntities.HashtagCount;
import s26901.pjatalks.Service.HashtagService;
import s26901.pjatalks.Service.PostService;

import java.security.Principal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/feed")
public class FeedViewController {
    private final PostService postService;
    private final HashtagService hashtagService;
    private final UserService userService;

    public FeedViewController(PostService postService, HashtagService hashtagService, UserService userService) {
        this.postService = postService;
        this.hashtagService = hashtagService;
        this.userService = userService;
    }

    @GetMapping
    public String getFeed(Model model,
//                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "5") int size){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            if (username != null) {
                Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
                if (userOutputDto.isPresent()){
                    boolean hasNewNotifications = userService.hasNewNotifications(userOutputDto.get().getId());
                    model.addAttribute("hasNewNotifications", hasNewNotifications);
                }
            }
        }

        // Add other model attributes
        Page<PostViewDto> postPage = postService.getViewPostDtos(0, size);
        Set<HashtagCount> topHashtags = hashtagService.getTrendingHashtags().getHashtag();
        model.addAttribute("postPage", postPage);
        model.addAttribute("topHash", topHashtags);
        model.addAttribute("size", size);
//        if (principal != null) {
//            String username = principal.getName();
//            // Assuming you have a service to fetch user details by username
//            Optional<UserOutputDto> user = userService.findByUsername(username);
//            user.ifPresent(userOutputDto -> model.addAttribute("user", userOutputDto));
//        }
        model.addAttribute("newPost", new PostInputDto());
        model.addAttribute("standardDate", new Date());
        model.addAttribute("personalized", false);
        return "feed";
    }

    @GetMapping("/personalized")
    public String getPersonalizedFeed(Model model,
                                      @RequestParam(defaultValue = "5") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            if (username != null) {
                Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
                if (userOutputDto.isPresent()) {
                    boolean hasNewNotifications = userService.hasNewNotifications(userOutputDto.get().getId());
                    model.addAttribute("hasNewNotifications", hasNewNotifications);

                    // Fetch posts from followed users
                    List<String> followedUserIds = userService.getFollowedUserIds(userOutputDto.get().getId());
                    Page<PostViewDto> postPage = postService.getPostsByUserIds(followedUserIds, 0, size);

                    model.addAttribute("postPage", postPage);
                    model.addAttribute("size", size);
                    model.addAttribute("newPost", new PostInputDto());
                    model.addAttribute("standardDate", new Date());
                    model.addAttribute("personalized", true);
                    return "feed"; // Ensure you have a view template for personalized feed
                }
            }
        }
        return "redirect:/login"; // Redirect to login if not authenticated
    }
}
