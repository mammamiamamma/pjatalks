package s26901.pjatalks.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s26901.pjatalks.DTO.General.LikeDto;
import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.Entity.Like;
import s26901.pjatalks.Entity.Post;
import s26901.pjatalks.Mapper.LikeMapper;
import s26901.pjatalks.Mapper.PostMapper;
import s26901.pjatalks.Repository.LikeRepository;
import s26901.pjatalks.Repository.PostRepository;
import s26901.pjatalks.Repository.UserRepository;

import java.util.*;

@Service
public class LikeService {
    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final Validator validator;

    public LikeService(LikeMapper likeMapper, PostMapper postMapper, LikeRepository likeRepository, UserRepository userRepository, PostRepository postRepository, Validator validator) {
        this.likeMapper = likeMapper;
        this.postMapper = postMapper;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.validator = validator;
    }

    public long getLikeCountForPost(String post_id){
        return likeRepository.countLikesByPostId(new ObjectId(post_id));
    }

    public List<PostOutputDto> getLikesByUser(String user_id){
        List<LikeDto> listOfLikes = likeRepository.getLikesByUser(new ObjectId(user_id))
                .stream()
                .map(likeMapper::map)
                .toList();
        List<PostOutputDto> listOfLikedPosts = new ArrayList<>();
        for (LikeDto likeDto : listOfLikes){
            Optional<Post> post = postRepository.findById(new ObjectId(likeDto.getPost_id()));
            post.ifPresent(p -> listOfLikedPosts.add(postMapper.map(p)));
        }
        return listOfLikedPosts;
    }

//    public String insertNewLike(LikeDto likeDto) throws AlreadyLikedException {
//        String user_id = likeDto.getUser_id();
//        String post_id = likeDto.getPost_id();
//        if (userRepository.findById(new ObjectId(user_id)).isEmpty())
//            throw new IllegalArgumentException("Invalid user_id");
//        if (postRepository.findById(new ObjectId(post_id)).isEmpty())
//            throw new IllegalArgumentException("Invalid post_id");
//        if (likeRepository.isAlreadyLiked(new ObjectId(user_id), new ObjectId(post_id))){
//            throw new AlreadyLikedException("This post is already liked by this user");
//        }
//        return likeRepository.insertLikeToPost(likeMapper.map(likeDto));
//    }

    @Transactional
    public boolean toggleLike(String post_id, String user_id, Date timestamp) {
        if (userRepository.findById(new ObjectId(user_id)).isEmpty())
            throw new IllegalArgumentException("Invalid user_id");
        if (postRepository.findById(new ObjectId(post_id)).isEmpty())
            throw new IllegalArgumentException("Invalid post_id");
        if (likeRepository.existsByPostIdAndUserId(new ObjectId(post_id), new ObjectId(user_id))) {
            likeRepository.deleteLikeFromPostByUser(new ObjectId(user_id), new ObjectId(post_id));
            return false;
        } else {
            Like like = new Like(user_id, post_id, timestamp);
            Set<ConstraintViolation<Like>> errors = validator.validate(like);
            if (!errors.isEmpty()) {
                throw new ConstraintViolationException(errors);
            }
            likeRepository.insertLikeToPost(like);
            return true;
        }
    }

    @Transactional
    public boolean deleteLikeFromPost(String user_id, String post_id){
        return likeRepository.deleteLikeFromPostByUser(new ObjectId(user_id), new ObjectId(post_id));
    }

    @Transactional
    public boolean deleteAllLikesByUser(String user_id){
        ObjectId transId = new ObjectId(user_id);
        if (userRepository.findById(transId).isEmpty())
            throw new IllegalArgumentException("Invalid user_id");
        return likeRepository.deleteAllLikesByUser(transId);
    }

    @Transactional
    public boolean deleteAllLikesByPost(String post_id){
        ObjectId transId = new ObjectId(post_id);
        if (postRepository.findById(transId).isEmpty())
            throw new IllegalArgumentException("Invalid user_id");
        return likeRepository.deleteAllLikesByPost(transId);
    }
}
