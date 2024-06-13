package s26901.pjatalks.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import s26901.pjatalks.SupportEntities.HashtagCount;

import java.util.Date;
import java.util.Set;

@Document(collection = "trendingHashtags")
public class TrendingHashtags {
    @Id
    private String id;
    private Set<HashtagCount> hashtag;
    private Date timestamp; // This can be used to store when the trending list was last updated

    public TrendingHashtags() {
    }

    public TrendingHashtags(String id, Set<HashtagCount> hashtag, Date timestamp) {
        this.id = id;
        this.hashtag = hashtag;
        this.timestamp = timestamp;
    }

    public TrendingHashtags(Set<HashtagCount> hashtag, Date timestamp) {
        this.hashtag = hashtag;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<HashtagCount> getHashtag() {
        return hashtag;
    }

    public void setHashtag(Set<HashtagCount> hashtag) {
        this.hashtag = hashtag;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
