package s26901.pjatalks.Service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.General.FollowingDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.FollowingViewDto;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Exception.RecordExistsException;
import s26901.pjatalks.Mapper.FollowingMapping;
import s26901.pjatalks.Mapper.UserMapper;
import s26901.pjatalks.Repository.FollowingRepository;
import s26901.pjatalks.Repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FollowingService {
    private final FollowingMapping followingMapping;
    private final FollowingRepository followingRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public FollowingService(FollowingMapping followingMapping, FollowingRepository followingRepository, UserRepository userRepository, UserMapper userMapper) {
        this.followingMapping = followingMapping;
        this.followingRepository = followingRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<String> getListOfFollowersForUser(String user_id){
        return followingRepository.getListOfFollowersIds(new ObjectId(user_id));
    }

    public List<String> getListOfFollowingForUser(String user_id){
        return followingRepository.getListOfFollowingIds(new ObjectId(user_id));
    }

    public FollowingViewDto getFollowingView(String user_id){
        FollowingViewDto followingViewDto = new FollowingViewDto();
        UserOutputDto userOutputDto = userRepository.findById(new ObjectId(user_id)).map(userMapper::map).orElseThrow();
        List<UserOutputDto> followersDto = new ArrayList<>();
        List<String> followerIds = followingRepository.getListOfFollowersIds(new ObjectId(user_id));
        for (String s : followerIds){
            Optional<User> optionalUser = userRepository.findById(new ObjectId(s));
            if (optionalUser.isPresent()) followersDto.add(userMapper.map(optionalUser.get()));
            else System.out.println("CRAZY");
        }
//        followingRepository.getListOfFollowersIds(new ObjectId(user_id)).forEach(userId ->
//                followersDto.add(userMapper.map(
//                        userRepository.findById(new ObjectId(userId))
//                                .orElse(
//                                        new User()
//                                )))
//        );
        List<UserOutputDto> followingDto = new ArrayList<>();
        followingRepository.getListOfFollowingIds(new ObjectId(user_id)).forEach(userId ->
                followingDto.add(userMapper.map(userRepository.findById(new ObjectId(userId)).orElse(new User())))
        );
        followingViewDto.setUser(userOutputDto);
        followingViewDto.setFollowers(followersDto);
        followingViewDto.setFollowing(followingDto);
        return followingViewDto;
    }

    public String addFollower(FollowingDto followingDto) throws RecordExistsException {
        if (followingRepository.isAlreadyFollowing(followingMapping.map(followingDto)))
            throw new RecordExistsException("Following record already exists");
        if (userRepository.findById(new ObjectId(followingDto.getF_follower_id())).isEmpty())
            throw new IllegalArgumentException("No user with id="+followingDto.getF_follower_id()+" found");
        if (userRepository.findById(new ObjectId(followingDto.getF_user_id())).isEmpty())
            throw new IllegalArgumentException("No user with id="+followingDto.getF_user_id()+" found");
        return followingRepository.addFollower(followingMapping.map(followingDto));
    }

    public boolean addFollower(String user_id, String target_id) throws RecordExistsException {
        if (doesUserFollowUser(user_id, target_id))
            throw new RecordExistsException("Following record already exists");
        if (userRepository.findById(new ObjectId(user_id)).isEmpty())
            throw new IllegalArgumentException("No user with id="+user_id+" found");
        if (userRepository.findById(new ObjectId(target_id)).isEmpty())
            throw new IllegalArgumentException("No user with id="+target_id+" found");
        return followingRepository.addFollower(user_id, target_id);
    }

    public boolean deleteFollowerFromUser(String follower_id, String user_id){
        return followingRepository.deleteFollowerFromUser(new ObjectId(follower_id), new ObjectId(user_id));
    }
    public boolean deleteFollowerFromUser(FollowingDto followingDto){
        return followingRepository.deleteFollowerFromUser(new ObjectId(followingDto.getF_follower_id()), new ObjectId(followingDto.getF_user_id()));
    }

    public boolean deleteAllFollowersForUser(String user_id){
        return followingRepository.deleteAllFollowersForUser(new ObjectId(user_id));
    }
    public boolean doesUserFollowUser(String user_id, String target_id){
        return followingRepository.doesUserFollowUser(new ObjectId(user_id), new ObjectId(target_id));
    }
}
