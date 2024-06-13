package s26901.pjatalks.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import s26901.pjatalks.Constraints.ObjectIdValidation;

import java.util.Date;

@Document(collection = "comments")
@TypeAlias("Comment")
public class Comment {
    @Id
    private String id;
    @NotBlank(message = "post_id is mandatory") //later internationalize messages
    @Indexed
    private String post_id;
    @NotBlank(message = "user_id is mandatory") //later internationalize messages
    @Indexed
    private String user_id;
    @NotBlank(message = "text is mandatory") //later internationalize messages
    private String text;
    @NotNull(message = "timestamp is mandatory")
    private Date timestamp;

    public Comment(String post_id, String user_id, String text, Date timestamp) {
        this.post_id = post_id;
        this.user_id = user_id;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Comment(String id, String post_id, String user_id, String text, Date timestamp) {
        this.id = id;
        this.post_id = post_id;
        this.user_id = user_id;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Comment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
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
}
