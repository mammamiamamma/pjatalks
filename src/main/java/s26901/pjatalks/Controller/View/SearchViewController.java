package s26901.pjatalks.Controller.View;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.PostViewDto;
import s26901.pjatalks.DTO.View.UserSearchDto;
import s26901.pjatalks.Service.FollowingService;
import s26901.pjatalks.Service.SearchService;
import s26901.pjatalks.Service.UserService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/search")
public class SearchViewController {
    private final SearchService searchService;
    private final FollowingService followingService;
    private final UserService userService;

    public SearchViewController(SearchService searchService, FollowingService followingService, UserService userService) {
        this.searchService = searchService;
        this.followingService = followingService;
        this.userService = userService;
    }

//    @GetMapping("/suggestions")
//    public ResponseEntity<List<String>> searchSuggestions(@RequestParam String query, Model model) {
//        List<String> suggestions = new ArrayList<>();
//        suggestions.add("Search for \"" + query + "\" in Users");
//        suggestions.add("Search for \"" + query + "\" in Posts");
//        return ResponseEntity.ok(suggestions);
//    }

//    @GetMapping("/users")
//    public String searchUsers(@RequestParam String query, Model model) {
//        List<String> suggestions = new ArrayList<>();
//        suggestions.add("Search for \"" + query + "\" in Users");
//        suggestions.add("Search for \"" + query + "\" in Posts");
//        return "search";
//    }
//
//    @GetMapping("/posts")
//    public String searchPosts(@RequestParam String query, Model model) {
//        List<String> suggestions = new ArrayList<>();
//        suggestions.add("Search for \"" + query + "\" in Users");
//        suggestions.add("Search for \"" + query + "\" in Posts");
//        return "search";
//    }

    @GetMapping("/results")
    public String searchResults(@RequestParam("query") String query, Model model) {
        List<UserSearchDto> users = searchService.searchUsers(query);
        List<PostViewDto> posts = searchService.searchPosts(query);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            if (username != null) {
                Optional<UserOutputDto> userOutputDto = userService.findByUsername(username);
                if (userOutputDto.isPresent()) {
                    for (UserSearchDto userSearchDto : users){
                        int followStatus = 0;
                        if (!userOutputDto.get().getId().equals(userSearchDto.user.getId())){
                            followStatus = followingService.doesUserFollowUser(
                                    userOutputDto.get().getId(),
                                    userSearchDto.user.getId()) ? 1 : -1;
                        }
                        userSearchDto.IsFollowed = followStatus;
                    }
                }
            }
        }

        model.addAttribute("users", users);
        model.addAttribute("posts", posts);
        model.addAttribute("query", query);

        return "search";
    }
}
