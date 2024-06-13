package s26901.pjatalks.DTO.View;

import s26901.pjatalks.DTO.Output.UserOutputDto;

public class UserSearchDto {
    public UserOutputDto user;
    public long followerCounter;
    public long followingCounter;
    public long postCount;
    public int IsFollowed; //0,1,-1

    public UserSearchDto(UserOutputDto user, long followerCounter, long followingCounter, long postCount, int IsFollowing) {
        this.user = user;
        this.followerCounter = followerCounter;
        this.followingCounter = followingCounter;
        this.postCount = postCount;
        this.IsFollowed = IsFollowing;
    }

    public UserSearchDto() {
    }

    public UserOutputDto getUser() {
        return user;
    }

    public void setUser(UserOutputDto user) {
        this.user = user;
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

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }

    public int isFollowed() {
        return IsFollowed;
    }

    public void setFollowed(int following) {
        IsFollowed = following;
    }
}
