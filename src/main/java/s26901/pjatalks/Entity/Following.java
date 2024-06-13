package s26901.pjatalks.Entity;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import s26901.pjatalks.Constraints.ObjectIdValidation;


@Document(collection = "following")
@TypeAlias("Following")
public class Following {
    @Id
    private String id;
    @NotBlank(message = "Follower's is mandatory") //later internationalize messages
    private String f_follower_id; // the person who 'caused' the notification
    @NotBlank(message = "'Followee' user_id is mandatory") //later internationalize messages
    private String f_user_id; // the person who 'caused' the notification

    public Following() {
    }

    public Following(String f_follower_id, String f_user_id) {
        this.f_follower_id = f_follower_id;
        this.f_user_id = f_user_id;
    }

    public Following(String id, String f_follower_id, String f_user_id) {
        this.id = id;
        this.f_follower_id = f_follower_id;
        this.f_user_id = f_user_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getF_follower_id() {
        return f_follower_id;
    }

    public void setF_follower_id(String f_follower_id) {
        this.f_follower_id = f_follower_id;
    }

    public String getF_user_id() {
        return f_user_id;
    }

    public void setF_user_id(String f_user_id) {
        this.f_user_id = f_user_id;
    }
}
