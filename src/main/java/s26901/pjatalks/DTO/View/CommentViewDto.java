package s26901.pjatalks.DTO.View;

import s26901.pjatalks.DTO.General.CommentDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;

public class CommentViewDto {
    private CommentDto comment;
    private UserOutputDto user;

    public CommentViewDto(CommentDto comment, UserOutputDto user) {
        this.comment = comment;
        this.user = user;
    }
    public CommentViewDto() {
    }

    public CommentDto getComment() {
        return comment;
    }

    public void setComment(CommentDto comment) {
        this.comment = comment;
    }

    public UserOutputDto getUser() {
        return user;
    }

    public void setUser(UserOutputDto user) {
        this.user = user;
    }
}
