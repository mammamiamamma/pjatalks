package s26901.pjatalks.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import s26901.pjatalks.Constraints.ObjectIdValidation;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Document(collection = "posts")
@TypeAlias("Post")
public class Post { //IMPLEMENT DTOS
    @Id
    private String id;
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

    public Post(String id, String user_id, String text, Date timestamp) {
        this.id = id;
        this.user_id = user_id;
        this.text = text;
        this.timestamp = timestamp;
        this.hashtags = extractHashtags();
    }

    public Post(String user_id, String text, Date timestamp) {
        this.user_id = user_id;
        this.text = text;
        this.timestamp = timestamp;
        this.hashtags = extractHashtags();
    }

    public Post() {
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
    public Set<String> extractHashtags() {
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(this.text);
        return matcher.results()
                .map(m -> m.group().toLowerCase().replace("#",""))
                .collect(Collectors.toSet());
    }
}
