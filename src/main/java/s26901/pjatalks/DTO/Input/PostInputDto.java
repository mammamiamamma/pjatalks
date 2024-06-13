package s26901.pjatalks.DTO.Input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import s26901.pjatalks.Constraints.ObjectIdValidation;

import java.util.Date;
import java.util.Set;

public class PostInputDto {
    @NotBlank(message = "author's user_id is mandatory") //later internationalize messages
    @ObjectIdValidation
    private String user_id;
    @NotBlank(message = "Empty posts are not allowed")
    @Length(max = 280)
    private String text;
    @NotNull(message = "Empty timestamps are not allowed")
    private Date timestamp;
    @Size(message="There can be no more than 10 hashtags in a post", max = 10)
    private Set<String> hashtags;
    public PostInputDto() {
    }

    public PostInputDto(String user_id, String text, Date timestamp) {
        this.user_id = user_id;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public Set<String> getHashtags() {
        return hashtags;
    }
    public void setHashtags(Set<String> hashtags) {
        this.hashtags = hashtags;
    }
}
