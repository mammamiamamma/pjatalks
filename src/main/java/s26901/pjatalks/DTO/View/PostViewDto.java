package s26901.pjatalks.DTO.View;

import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;

public class PostViewDto {
    private String post_id;
    private PostOutputDto post;
    private UserOutputDto user;
    private boolean isAlreadyLikedByUser;
    private long likeCount;
    private long commentCount;
    public PostViewDto(String postId, PostOutputDto post, UserOutputDto user, long likeCount, long commentCount, boolean isAlreadyLiked) {
        this.post_id = postId;
        this.post = post;
        this.user = user;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isAlreadyLikedByUser = isAlreadyLiked;
    }

    public PostViewDto() {
    }
    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public PostOutputDto getPost() {
        return post;
    }

    public void setPost(PostOutputDto post) {
        this.post = post;
    }

    public UserOutputDto getUser() {
        return user;
    }

    public void setUser(UserOutputDto user) {
        this.user = user;
    }

    public boolean isAlreadyLikedByUser() {
        return isAlreadyLikedByUser;
    }

    public void setAlreadyLikedByUser(boolean alreadyLikedByUser) {
        isAlreadyLikedByUser = alreadyLikedByUser;
    }
}
