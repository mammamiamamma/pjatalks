package s26901.pjatalks.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s26901.pjatalks.DTO.Input.PostInputDto;
import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.PostViewDto;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Mapper.PostMapper;
import s26901.pjatalks.Mapper.UserMapper;
import s26901.pjatalks.Repository.CommentRepository;
import s26901.pjatalks.Repository.LikeRepository;
import s26901.pjatalks.Repository.PostRepository;
import s26901.pjatalks.Entity.Post;
import s26901.pjatalks.Repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final Validator validator;

    public PostService(PostMapper postMapper, UserMapper userMapper, PostRepository postRepository, UserRepository userRepository, LikeRepository likeRepository, CommentRepository commentRepository, Validator validator) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.validator = validator;
    }
    public Page<PostViewDto> getViewPostDtos(int page, int size, UserOutputDto userOutputDto) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findAll(pageRequest);
        List<PostViewDto> postDTOs = postPage.getContent().stream()
                .map(post -> getViewFromPost(post, userOutputDto))
                .collect(Collectors.toList());

        return new PageImpl<>(postDTOs, pageRequest, postPage.getTotalElements());
    }

    private PostViewDto getViewFromPost(Post post, UserOutputDto userOutputDto) {
        User user = userRepository.findById(new ObjectId(post.getUser_id())).orElse(null);
        UserOutputDto userFromPostDto = user != null ? userMapper.map(user) : createAnonymousUser();

        boolean isAlreadyLiked = userOutputDto != null && likeRepository.isAlreadyLiked(new ObjectId(userOutputDto.getId()), new ObjectId(post.getId()));
        long likeCount = likeRepository.countLikesByPostId(new ObjectId(post.getId()));
        long commentCount = commentRepository.countCommentsByPost(new ObjectId(post.getId()));

        return new PostViewDto(post.getId(), postMapper.map(post), userFromPostDto, likeCount, commentCount, isAlreadyLiked);
    }

    private UserOutputDto createAnonymousUser() {
        UserOutputDto userFromPostDto = new UserOutputDto();
        userFromPostDto.setUsername("Anon");
        return userFromPostDto;
    }

    public List<PostOutputDto> getPostsByUserId(String id){
        return postRepository.findByUserId(new ObjectId(id)).stream().map(postMapper::map).toList();
    }

    public PostOutputDto getPostById(String id) {
        return postRepository.findById(new ObjectId(id))
                .map(postMapper::map)
                .orElse(null);
    }

    public PostViewDto getPostByIdView(String id, UserOutputDto userOutputDto) {
        return postRepository.findById(new ObjectId(id))
                .map(post -> getViewFromPost(post, userOutputDto))
                .orElse(null);
    }

    public PostViewDto insertPost(PostInputDto postDto) {
        if (userRepository.findById(new ObjectId(postDto.getUser_id())).isEmpty()) {
            throw new IllegalArgumentException("Invalid author's user_id");
        }

        Post post = postMapper.map(postDto);
        validatePost(post);

        String post_id = postRepository.insertPost(post);
        if (post_id == null) {
            return null;
        }

        return createPostViewDto(post, postDto.getUser_id(), post_id);
    }

    private void validatePost(Post post) {
        Set<ConstraintViolation<Post>> errorsPost = validator.validate(post);
        if (!errorsPost.isEmpty()) {
            throw new ConstraintViolationException("Validation for Post object failed", errorsPost);
        }
    }

    private PostViewDto createPostViewDto(Post post, String userId, String postId) {
        User user = userRepository.findById(new ObjectId(userId)).orElseThrow(() -> new IllegalArgumentException("No user with such id found"));

        PostViewDto viewPost = new PostViewDto();
        viewPost.setPost_id(postId);
        viewPost.setPost(postMapper.map(post));
        viewPost.setUser(userMapper.map(user));
        viewPost.setLikeCount(0);
        viewPost.setCommentCount(0);

        return viewPost;
    }

    @Transactional
    public void deletePostsByUserId(String userId) throws NotAcknowledgedException {
        List<Post> postsForDeletion = postRepository.findByUserId(new ObjectId(userId));
        for (Post post : postsForDeletion) {
            deletePostDependencies(post.getId());
            postRepository.deletePost(new ObjectId(post.getId()));
        }
    }

    @Transactional
    public void deletePost(String postId) throws NotAcknowledgedException {
        deletePostDependencies(postId);
        postRepository.deletePost(new ObjectId(postId));
    }

    private void deletePostDependencies(String postId) throws NotAcknowledgedException {
        if (!likeRepository.deleteAllLikesByPost(new ObjectId(postId))) {
            throw new NotAcknowledgedException("Deletion from 'likes' not acknowledged by database");
        }
        if (!commentRepository.deleteCommentsOfPost(new ObjectId(postId))) {
            throw new NotAcknowledgedException("Deletion from 'comments' not acknowledged by database");
        }
    }

    public Page<PostViewDto> getPostsByUserIds(List<String> followedUserIds, int page, int size, UserOutputDto userOutputDto) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<ObjectId> idToObjectIds = followedUserIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());
        Page<Post> postPage = postRepository.findByUserIds(idToObjectIds, pageRequest);
        List<PostViewDto> postDTOs = postPage.getContent().stream()
                .map(post -> getViewFromPost(post, userOutputDto))
                .collect(Collectors.toList());

        return new PageImpl<>(postDTOs, pageRequest, postPage.getTotalElements());
    }
}
