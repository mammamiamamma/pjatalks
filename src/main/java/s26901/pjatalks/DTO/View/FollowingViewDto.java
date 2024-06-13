package s26901.pjatalks.DTO.View;

import s26901.pjatalks.DTO.Output.UserOutputDto;

import java.util.List;

public class FollowingViewDto {
    public UserOutputDto user;
    public List<UserOutputDto> followers;
    public List<UserOutputDto> following;

    public FollowingViewDto() {
    }

    public FollowingViewDto(UserOutputDto user, List<UserOutputDto> followers, List<UserOutputDto> following) {
        this.user = user;
        this.followers = followers;
        this.following = following;
    }

    public UserOutputDto getUser() {
        return user;
    }

    public void setUser(UserOutputDto user) {
        this.user = user;
    }

    public List<UserOutputDto> getFollowers() {
        return followers;
    }

    public void setFollowers(List<UserOutputDto> followers) {
        this.followers = followers;
    }

    public List<UserOutputDto> getFollowing() {
        return following;
    }

    public void setFollowing(List<UserOutputDto> following) {
        this.following = following;
    }
}
