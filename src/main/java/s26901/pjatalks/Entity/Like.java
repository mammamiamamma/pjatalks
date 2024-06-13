package s26901.pjatalks.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import s26901.pjatalks.Constraints.ObjectIdValidation;

import java.util.Date;

@Document(collection = "likes")
@TypeAlias("Like")
public class Like {
    @Id
    private String id;
    @NotBlank(message = "User ID is mandatory")
    @Indexed
    private String user_id;

    @NotBlank(message = "Post ID is mandatory")
    @Indexed
    private String post_id;

    @NotNull(message = "Timestamp is mandatory")
    private Date timestamp;

    public Like() {
    }

    public Like(String id, String user_id, String post_id, Date timestamp) {
        this.id = id;
        this.user_id = user_id;
        this.post_id = post_id;
        this.timestamp = timestamp;
    }

    public Like(String user_id, String post_id, Date timestamp) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
