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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        Page<Post> postPage = postRepository.findAll(pageRequest); //need that
        List<Post> posts = postPage.getContent();
        List<PostViewDto> postDTOs = new ArrayList<>();

        for (Post post : posts) {
            postDTOs.add(getViewFromPost(post, userOutputDto));
        }

        return new PageImpl<>(postDTOs, pageRequest, postPage.getTotalElements());
    }

    private PostViewDto getViewFromPost(Post post, UserOutputDto userOutputDto){
        User user = userRepository.findById(new ObjectId(post.getUser_id())).orElse(null);
        UserOutputDto userFromPostDto;
        boolean isAlreadyLiked = false;
        if (userOutputDto != null){
//            userOutput = userMapper.map(user.get());
            isAlreadyLiked = likeRepository.isAlreadyLiked(new ObjectId(userOutputDto.getId()), new ObjectId(post.getId()));
        } if (user != null){
            userFromPostDto = userMapper.map(user);
        } else {
            userFromPostDto = new UserOutputDto();
            userFromPostDto.setUsername("Anon");
        }
        long likeCount = likeRepository.countLikesByPostId(new ObjectId(post.getId()));
        long commentCount = commentRepository.countCommentsByPost(new ObjectId(post.getId()));
        return new PostViewDto(post.getId(), postMapper.map(post), userFromPostDto, likeCount, commentCount, isAlreadyLiked);
    }
    public List<PostOutputDto> getPostsByUserId(String id){
        return postRepository.findByUserId(new ObjectId(id)).stream().map(postMapper::map).toList();
    }

    public PostOutputDto getPostByIdApi(String id){
        Optional<Post> post = postRepository.findById(new ObjectId(id));
        return post.map(postMapper::map).orElse(null);
    }

    public PostViewDto getPostByIdView(String id, UserOutputDto userOutputDto){
        Optional<Post> postOpt = postRepository.findById(new ObjectId(id));
        if (postOpt.isPresent()){
            Post post = postOpt.get();
            return getViewFromPost(post, userOutputDto);
        } else return null;
    }

    public PostViewDto insertPost(PostInputDto postDto){
        String user_id = postDto.getUser_id();
        if (userRepository.findById(new ObjectId(user_id)).isEmpty())
            throw new IllegalArgumentException("Invalid author's user_id");
        //maybe make timestamp = now here instead of in the form?
//        if (post.getTimestamp().before(Date.from(Instant.now())))
//            throw new IllegalArgumentException("Timestamp can't be earlier than current time");
        Post post = postMapper.map(postDto);
        Set<ConstraintViolation<Post>> errorsPost = validator.validate(post);
        if (errorsPost.isEmpty()){
            PostViewDto viewPost = new PostViewDto();
            String post_id = postRepository.insertPost(postMapper.map(postDto));
            if (post_id == null) return null;
            viewPost.setPost_id(post_id);
            viewPost.setPost(postMapper.map(post));
            Optional<User> userOptional = userRepository.findById(new ObjectId(postDto.getUser_id()));
            if (userOptional.isPresent()) {
                viewPost.setUser(userMapper.map(userOptional.get()));
            } else throw new IllegalArgumentException("No user with such id found");
            viewPost.setLikeCount(0);
            viewPost.setCommentCount(0);
            return viewPost;
        }
        else throw new ConstraintViolationException("Validation for Post object failed", errorsPost);
    }

    @Transactional
    public void deletePostsByUserId(String user_id) throws NotAcknowledgedException {
        List<Post> postsForDeletion = postRepository.findByUserId(new ObjectId(user_id));
        for (Post post : postsForDeletion){
            commentRepository.deleteCommentsOfPost(new ObjectId(post.getId()));
            likeRepository.deleteAllLikesByPost(new ObjectId(post.getId()));
            postRepository.deletePost(new ObjectId(post.getId()));
        }
    }

    @Transactional
    public boolean deletePost(String post_id) throws NotAcknowledgedException {
        if (!likeRepository.deleteAllLikesByPost(new ObjectId(post_id)))
            throw new NotAcknowledgedException("Deletion from 'likes' not acknowledged by database");
        if (!commentRepository.deleteCommentsOfPost(new ObjectId(post_id)))
            throw new NotAcknowledgedException("Deletion from 'likes' not acknowledged by database");
        return postRepository.deletePost(new ObjectId(post_id));
    }

    public Page<PostViewDto> getPostsByUserIds(List<String> followedUserIds, int page, int size, UserOutputDto userOutputDto) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<ObjectId> idToObjectIds = followedUserIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());
        Page<Post> postPage = postRepository.findByUserIds(idToObjectIds, pageRequest);
        List<Post> posts = postPage.getContent();
        List<PostViewDto> postDTOs = posts.stream()
                .map(post -> getViewFromPost(post, userOutputDto))
                .collect(Collectors.toList());

        return new PageImpl<>(postDTOs, pageRequest, postPage.getTotalElements());
    }
}
