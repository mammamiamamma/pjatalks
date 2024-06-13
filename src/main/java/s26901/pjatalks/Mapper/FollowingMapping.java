package s26901.pjatalks.Mapper;

import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.General.FollowingDto;
import s26901.pjatalks.Entity.Following;

@Service
public class FollowingMapping {
    public Following map(FollowingDto following){
        return new Following(
                following.getF_follower_id(),
                following.getF_user_id()
        );
    }

    public FollowingDto map(Following followingDto){
        return new FollowingDto(
                followingDto.getF_follower_id(),
                followingDto.getF_user_id()
        );
    }
}
