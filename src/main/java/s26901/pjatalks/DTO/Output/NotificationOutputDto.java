package s26901.pjatalks.DTO.Output;

import java.util.Date;

public class NotificationOutputDto {
    private String _id;
    private String content;
    private Date timestamp;
    private String user_id; // the person who 'caused' the notification
    private String causer_id; // the person who 'caused' the notification
    private String type;

    public NotificationOutputDto(String id, String content, Date timestamp, String user_id, String causer_id, String type) {
        _id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.causer_id = causer_id;
        this.type = type;
    }

    public NotificationOutputDto() {
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

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
