package s26901.pjatalks.DTO.General;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import s26901.pjatalks.Constraints.ObjectIdValidation;

import java.util.Date;

public class NotificationDto {
    @NotBlank(message = "Notifications with no content are not allowed")
    private String content;
    @NotNull(message = "No timestamp for notification")
    private Date timestamp;
    @NotBlank(message = "Notification's user_id is mandatory") //later internationalize messages
    @ObjectIdValidation
    private String user_id; // the person who 'caused' the notification
    @NotBlank(message = "Notification's causer_id is mandatory") //later internationalize messages
    @ObjectIdValidation
    private String causer_id; // the person who 'caused' the notification
    @NotBlank(message = "Notification's type is mandatory")
    private String type;

    public NotificationDto() {
    }

    public NotificationDto(String content, Date timestamp, String user_id, String causer_id, String type) {
        this.content = content;
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.causer_id = causer_id;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCauser_id() {
        return causer_id;
    }
    public void setCauser_id(String causer_id) {
        this.causer_id = causer_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
