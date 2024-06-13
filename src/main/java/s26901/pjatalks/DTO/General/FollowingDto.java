package s26901.pjatalks.DTO.General;

import jakarta.validation.constraints.NotBlank;
import s26901.pjatalks.Constraints.ObjectIdValidation;

public class FollowingDto {
    @NotBlank(message = "Follower's is mandatory") //later internationalize messages
    @ObjectIdValidation
    private String f_follower_id; // the person who 'caused' the notification
    @NotBlank(message = "'Followee' user_id is mandatory") //later internationalize messages
    @ObjectIdValidation
    private String f_user_id; // the person who 'caused' the notification

    public FollowingDto() {
    }

    public FollowingDto(String f_follower_id, String f_user_id) {
        this.f_follower_id = f_follower_id;
        this.f_user_id = f_user_id;
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
