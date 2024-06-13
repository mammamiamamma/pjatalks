package s26901.pjatalks.DTO.View;

import org.springframework.data.domain.Page;
import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;

import java.util.List;
import java.util.Set;

public class UserViewDto {
    public UserOutputDto user;
    public List<PostViewDto> posts;
    public long followerCounter;
    public long followingCounter;

    public UserViewDto(UserOutputDto user, List<PostViewDto> posts, int followerCounter, int followingCounter) {
        this.user = user;
        this.posts = posts;
        this.followerCounter = followerCounter;
        this.followingCounter = followingCounter;
    }

    public UserViewDto() {
    }

    public UserOutputDto getUser() {
        return user;
    }

    public void setUser(UserOutputDto user) {
        this.user = user;
    }

    public List<PostViewDto> getPosts() {
        return posts;
    }

    public void setPosts(List<PostViewDto> posts) {
        this.posts = posts;
    }

    public long getFollowerCounter() {
        return followerCounter;
    }

    public void setFollowerCounter(long followerCounter) {
        this.followerCounter = followerCounter;
    }

    public long getFollowingCounter() {
        return followingCounter;
    }

    public void setFollowingCounter(long followingCounter) {
        this.followingCounter = followingCounter;
    }
}
