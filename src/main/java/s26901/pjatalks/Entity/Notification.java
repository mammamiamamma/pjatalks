package s26901.pjatalks.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "notifications")
@TypeAlias("Notifications")
public class Notification {
    @Id
    private String id;
    @NotBlank(message = "Notifications with no content are not allowed")
    private String content;
    @NotNull(message = "No timestamp for notification")
    private Date timestamp;
    @NotBlank(message = "Notification's causer_id is mandatory") //later internationalize messages
    private String causer_id; // the person who 'caused' the notification
    @NotBlank(message = "Notification's causer_id is mandatory") //later internationalize messages
    @Indexed
    private String user_id;
    @NotBlank(message = "Notification's type is mandatory")
    private String type;

    public Notification() {
    }

    public Notification(String content, Date timestamp, String user_id, String causerId, String type) {
        this.content = content;
        this.timestamp = timestamp;
        this.causer_id = causerId;
        this.user_id = user_id;
        this.type = type;
    }

    public Notification(String id, String content, Date timestamp, String causerId, String user_id, String type) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.causer_id = causerId;
        this.user_id = user_id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
