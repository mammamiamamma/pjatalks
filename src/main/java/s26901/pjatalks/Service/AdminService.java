package s26901.pjatalks.Service;

import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Mapper.UserMapper;
import s26901.pjatalks.Repository.*;

import java.util.*;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserMapper userMapper;

    public AdminService(UserRepository userRepository, PostRepository postRepository, LikeRepository likeRepository, CommentRepository commentRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.userMapper = userMapper;
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalPosts() {
        return postRepository.count();
    }

    public long getTotalComments() {
        return commentRepository.count();
    }

    public long getActiveUsers() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        Date twoDaysAgo = calendar.getTime();
        return userRepository.countActiveUsers(twoDaysAgo);
    }

    public Map<UserOutputDto, Integer> getTopUsersByPosts() {
        Map<User, Integer> userMap = userRepository.findUsers(postRepository.findTopUsersByPosts());
        return sortUserMap(userMap);
    }

    public Map<UserOutputDto, Integer> getTopUsersByLikes() {
        Map<User, Integer> userMap = userRepository.findUsers(likeRepository.findTop3LikingUsers());
        return sortUserMap(userMap);
    }

    private Map<UserOutputDto, Integer> sortUserMap(Map<User, Integer> userMap){
        Map<UserOutputDto, Integer> resultMap = new HashMap<>();

        // Map User to UserOutputDto
        for (User user : userMap.keySet()) {
            resultMap.put(userMapper.map(user), userMap.get(user));
        }

        // Create a list from the entries of resultMap
        List<Map.Entry<UserOutputDto, Integer>> entryList = new ArrayList<>(resultMap.entrySet());

        // Sort the list by values in descending order
        entryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Insert the sorted entries into a LinkedHashMap to preserve order
        Map<UserOutputDto, Integer> sortedResultMap = new LinkedHashMap<>();
        for (Map.Entry<UserOutputDto, Integer> entry : entryList) {
            sortedResultMap.put(entry.getKey(), entry.getValue());
        }

        return sortedResultMap;
    }
}
