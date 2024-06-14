package s26901.pjatalks.Service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.PostViewDto;
import s26901.pjatalks.DTO.View.UserSearchDto;
import s26901.pjatalks.Entity.Post;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Mapper.PostMapper;
import s26901.pjatalks.Mapper.UserMapper;
import s26901.pjatalks.Repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FollowingRepository followingRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    public SearchService(UserRepository userRepository, PostRepository postRepository, FollowingRepository followingRepository, LikeRepository likeRepository, CommentRepository commentRepository, UserMapper userMapper, PostMapper postMapper) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followingRepository = followingRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
    }

    public List<UserSearchDto> searchUsers(String query) {
        List<User> users = userRepository.findUsersByUsernameContaining(query);
        List<UserSearchDto> resultList = new ArrayList<>();
        for (User user : users){
            resultList.add(getSearchDtoFromUser(user));
        }
        return resultList;
    }

    private UserSearchDto getSearchDtoFromUser(User user){
        UserOutputDto userOutputDto = userMapper.map(user);
        UserSearchDto userSearchDto = new UserSearchDto();
        userSearchDto.setUser(userOutputDto);
        userSearchDto.setFollowerCounter(
                followingRepository.getFollowerCountForUser(
                        new ObjectId(userOutputDto.getId())));
        userSearchDto.setFollowingCounter(
                followingRepository.getFollowingCountForUser(
                        new ObjectId(userOutputDto.getId())));
        return userSearchDto;
    }

    public List<UserOutputDto> searchUsersForSuggestions(String query) {
        List<User> users = userRepository.findUsersForSuggestionsByUsernameContaining(query);
        return users.stream().map(userMapper::map).collect(Collectors.toList());
    }

    public List<PostViewDto> searchPosts(String query, UserOutputDto userOutputDto) {
        List<Post> posts = postRepository.findPostsByTextContaining(query);
        List<PostViewDto> resultList = new ArrayList<>();
        for (Post post : posts){
            resultList.add(getViewDtoFromPost(post, userOutputDto));
        }
        return resultList;
    }

    private PostViewDto getViewDtoFromPost(Post post, UserOutputDto userOutputDto){
//        Optional<User> user = userRepository.findById(new ObjectId(post.getUser_id()));
//        if (user.isPresent()){
//            UserOutputDto userOutputDto = userMapper.map(user.get());
            PostViewDto postViewDto = new PostViewDto();
            postViewDto.setPost_id(post.getId());
            postViewDto.setUser(userOutputDto);
            postViewDto.setPost(postMapper.map(post));
            postViewDto.setLikeCount(
                    likeRepository.countLikesByPostId(
                            new ObjectId(post.getId())
                    )
            );
            postViewDto.setCommentCount(
                    commentRepository.countCommentsByPost(
                            new ObjectId(post.getId())
                    )
            );
            postViewDto.setAlreadyLikedByUser(
                    likeRepository.isAlreadyLiked(new ObjectId(userOutputDto.getId()), new ObjectId(post.getId()))
            );
            return postViewDto;
//        }
//        return null;
    }
}
