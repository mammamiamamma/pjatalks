package s26901.pjatalks.DTO.General;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import s26901.pjatalks.Constraints.ObjectIdValidation;

import java.util.Date;

public class LikeDto {
    @NotBlank(message = "User ID is mandatory")
    @ObjectIdValidation
    private String user_id;

    @NotBlank(message = "Post ID is mandatory")
    @ObjectIdValidation
    private String post_id;

    @NotNull(message = "Timestamp is mandatory")
    private Date timestamp;

    public LikeDto() {
    }

    public LikeDto(String user_id, String post_id, Date timestamp) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
