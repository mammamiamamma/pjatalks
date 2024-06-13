package s26901.pjatalks.DTO.Output;

import java.util.Date;
import java.util.Set;

public class PostOutputDto {
    private String user_id;
    private String text;
    private Date timestamp;
    public PostOutputDto() {
    }
    public PostOutputDto(String user_id, String text, Date timestamp) {
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
}
