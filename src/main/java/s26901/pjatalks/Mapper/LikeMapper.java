package s26901.pjatalks.Mapper;

import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.General.LikeDto;
import s26901.pjatalks.Entity.Like;

@Service
public class LikeMapper {
    public Like map(LikeDto likeDto){
        return new Like(
             likeDto.getUser_id(),
             likeDto.getPost_id(),
             likeDto.getTimestamp()
        );
    }

    public LikeDto map(Like like){
        return new LikeDto(
                like.getUser_id(),
                like.getPost_id(),
                like.getTimestamp()
        );
    }
}
