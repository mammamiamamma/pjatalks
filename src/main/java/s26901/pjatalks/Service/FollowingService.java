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

import java.util.*;
import java.util.stream.Collectors;

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

//    public List<String> getListOfFollowersForUser(String user_id){
//        return followingRepository.getListOfFollowersIds(new ObjectId(user_id));
//    }
//
//    public List<String> getListOfFollowingForUser(String user_id){
//        return followingRepository.getListOfFollowingIds(new ObjectId(user_id));
//    }

    public FollowingViewDto getFollowingView(UserOutputDto userOutputDto) {
        FollowingViewDto followingViewDto = new FollowingViewDto();

        ObjectId userId = new ObjectId(userOutputDto.getId());

        List<String> followerIds = followingRepository.getListOfFollowersIds(userId);
        List<String> followingIds = followingRepository.getListOfFollowingIds(userId);

        List<ObjectId> allUserIds = new ArrayList<>();
        allUserIds.addAll(followerIds.stream().map(ObjectId::new).toList());
        allUserIds.addAll(followingIds.stream().map(ObjectId::new).toList());

        List<User> users = userRepository.findUsers(allUserIds);
        Map<String, UserOutputDto> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, userMapper::map));

        List<UserOutputDto> followersDto = followerIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<UserOutputDto> followingDto = followingIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .toList();

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

//    public boolean addFollower(String user_id, String target_id) throws RecordExistsException {
//        if (doesUserFollowUser(user_id, target_id))
//            throw new RecordExistsException("Following record already exists");
//        if (userRepository.findById(new ObjectId(user_id)).isEmpty())
//            throw new IllegalArgumentException("No user with id="+user_id+" found");
//        if (userRepository.findById(new ObjectId(target_id)).isEmpty())
//            throw new IllegalArgumentException("No user with id="+target_id+" found");
//        return followingRepository.addFollower(user_id, target_id);
//    }

//    public boolean deleteFollowerFromUser(String follower_id, String user_id){
//        return followingRepository.deleteFollowerFromUser(new ObjectId(follower_id), new ObjectId(user_id));
//    }
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
