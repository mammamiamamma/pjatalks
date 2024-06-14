package s26901.pjatalks.Mapper;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.Input.UserInputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Exception.RoleNotFoundException;

@Service
public class UserMapper {
    @Autowired
    private RoleMapper roleMapper;

    public User map(UserInputDto userInputDto) throws RoleNotFoundException {
        return new User(
                new ObjectId().toHexString(),
                userInputDto.getUsername(),
                userInputDto.getPassword(),
                userInputDto.getEmailAddress(),
                userInputDto.getShortBio(),
                roleMapper.mapDto(userInputDto.getUserRoles()));
    }

    public UserOutputDto map(User user){
        return new UserOutputDto(
                user.getId(),
                user.getUsername(),
                user.getShortBio(), //check for isBlank?
                roleMapper.mapEnt(user.getRoles()));
    }
}
