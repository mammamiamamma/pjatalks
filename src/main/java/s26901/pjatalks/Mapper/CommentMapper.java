package s26901.pjatalks.Mapper;

import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.General.CommentDto;
import s26901.pjatalks.Entity.Comment;

import java.util.Date;

@Service
public class CommentMapper {
    public Comment map(CommentDto commentDto){
        return new Comment(
                commentDto.getPost_id(),
                commentDto.getUser_id(),
                commentDto.getText(),
                commentDto.getTimestamp()
        );
    }

    public CommentDto map(Comment comment){
        return new CommentDto(
                comment.getPost_id(),
                comment.getUser_id(),
                comment.getText(),
                comment.getTimestamp()
        );
    }
}
