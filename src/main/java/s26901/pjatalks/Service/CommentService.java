package s26901.pjatalks.Service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.General.CommentDto;
import s26901.pjatalks.DTO.View.CommentViewDto;
import s26901.pjatalks.Entity.Comment;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Mapper.CommentMapper;
import s26901.pjatalks.Mapper.UserMapper;
import s26901.pjatalks.Repository.CommentRepository;
import s26901.pjatalks.Repository.PostRepository;
import s26901.pjatalks.Repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentService(UserRepository userRepository, CommentMapper commentMapper, UserMapper userMapper, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public List<CommentDto> findAllByPostApi(String post_id){
//        if (!ObjectId.isValid(post_id)) {
//            throw new IllegalArgumentException("Invalid ObjectId: " + post_id);
//        }
        ObjectId postObjectId = new ObjectId(post_id);
        if (postRepository.findById(postObjectId).isEmpty()) //do we need it?
            throw new IllegalArgumentException("No post with such id found!");
        return commentRepository.findAllByPost(postObjectId).stream().map(commentMapper::map).toList();
    }

    public List<CommentViewDto> findAllByPostView(String post_id){
//        if (!ObjectId.isValid(post_id)) {
//            throw new IllegalArgumentException("Invalid ObjectId: " + post_id);
//        }
        ObjectId postObjectId = new ObjectId(post_id);
        if (postRepository.findById(postObjectId).isEmpty()) //do we need it?
            throw new IllegalArgumentException("No post with such id found!");
        List<CommentViewDto> commentViewDtos = new ArrayList<>();
        List<CommentDto> commentList = commentRepository.findAllByPost(postObjectId).stream().map(commentMapper::map).toList();
        for (CommentDto comment: commentList){
            commentViewDtos.add(new CommentViewDto(
                    comment,
                    userMapper.map(userRepository.findById(new ObjectId(comment.getUser_id())).get()))); //kinda hoping that the comment has a user
        }
        return commentViewDtos;
    }



    public List<CommentDto> findAllByUser(String user_id){
//        if (!ObjectId.isValid(user_id)) { //make it into annotation
//            throw new IllegalArgumentException("Invalid ObjectId: " + user_id);
//        }
        ObjectId userObjectId = new ObjectId(user_id);
        if (userRepository.findById(userObjectId).isEmpty()) //do we need it?
            throw new IllegalArgumentException("No user with such id found!");
        return commentRepository.findAllByUser(userObjectId).stream().map(commentMapper::map).toList();
    }

    public boolean deleteCommentFromPost(String user_id, String post_id){
//        if (!ObjectId.isValid(user_id))
//            throw new IllegalArgumentException("Invalid ObjectId: " + user_id);
//        if (!ObjectId.isValid(post_id))
//            throw new IllegalArgumentException("Invalid ObjectId: " + post_id);
        return commentRepository.deleteCommentFromPost(new ObjectId(user_id), new ObjectId(post_id));
    }

    public boolean deleteCommentsByUser(String user_id){
//        if (!ObjectId.isValid(user_id))
//            throw new IllegalArgumentException("Invalid ObjectId: " + user_id);
        return commentRepository.deleteCommentsByUser(new ObjectId(user_id));
    }

    public boolean deleteCommentsOfPost(String post_id){
//        if (!ObjectId.isValid(post_id))
//            throw new IllegalArgumentException("Invalid ObjectId: " + post_id);
        return commentRepository.deleteCommentsOfPost(new ObjectId(post_id));
    }

    public CommentViewDto insertCommentToPost(CommentDto comment) throws NotAcknowledgedException {
//        if (!ObjectId.isValid(comment.getPost_id()))
//            throw new IllegalArgumentException("Invalid ObjectId: " + comment.getPost_id());
//        if (!ObjectId.isValid(comment.getUser_id()))
//            throw new IllegalArgumentException("Invalid ObjectId: " + comment.getUser_id());
        String resultId = commentRepository.insertCommentToPost(commentMapper.map(comment));
        if (resultId != null) {
            CommentViewDto commentViewDto = new CommentViewDto();
            commentViewDto.setComment(comment);
            Optional<User> userOptional = userRepository.findById(new ObjectId(comment.getUser_id()));
            if (userOptional.isPresent()){
                commentViewDto.setUser(userMapper.map(userOptional.get()));
            } else throw new IllegalArgumentException("No user with such id found");
            return commentViewDto;
        } else throw new NotAcknowledgedException("Comment insert was not acknowledged");
    }
}
