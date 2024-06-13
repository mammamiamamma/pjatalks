package s26901.pjatalks.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.Input.RoleDto;
import s26901.pjatalks.Entity.UserRole;
import s26901.pjatalks.Exception.RoleNotFoundException;
import s26901.pjatalks.Repository.RolesRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class RoleMapper {
    @Autowired
    private RolesRepository rolesRepository;
    public Set<UserRole> mapDto(Set<RoleDto> roleDto) throws RoleNotFoundException{
        Set<UserRole> resultSet = new HashSet<>();
        for (RoleDto role : roleDto){
            UserRole userRole = rolesRepository.findByName(role.getName());
            if (userRole == null) {
                throw new RoleNotFoundException("Role not found: " + role.getName());
            }
            resultSet.add(userRole);
        }
        return resultSet;
    }

    public Set<RoleDto> mapEnt(Set<UserRole> roles){
        Set<RoleDto> resultSet = new HashSet<>();
        for (UserRole role : roles){
            resultSet.add(new RoleDto(role.getName()));
        }
        return resultSet;
    }

    public UserRole map(RoleDto roleDto){
        return new UserRole(roleDto.getName());
    }

    public RoleDto map(UserRole role){
        return new RoleDto(role.getName());
    }
}
