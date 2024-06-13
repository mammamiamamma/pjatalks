package s26901.pjatalks.Mapper;

import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.Input.PostInputDto;
import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.Entity.Post;

@Service
public class PostMapper {
    public Post map(PostInputDto postDto){
        return new Post(
                postDto.getUser_id(),
                postDto.getText(),
                postDto.getTimestamp()
        );
    }

    public PostOutputDto map(Post post){
        return new PostOutputDto(
                post.getUser_id(),
                post.getText(),
                post.getTimestamp()
        );
    }
}
