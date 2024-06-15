package s26901.pjatalks.Service;

import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.Input.RoleDto;
import s26901.pjatalks.Mapper.RoleMapper;
import s26901.pjatalks.Repository.RolesRepository;

@Service
public class RolesService {
    private final RolesRepository rolesRepository;
    private final RoleMapper roleMapper;

    public RolesService(RolesRepository rolesRepository, RoleMapper roleMapper) {
        this.rolesRepository = rolesRepository;
        this.roleMapper = roleMapper;
    }

    public String saveRole(RoleDto roleDto){
        if (rolesRepository.findByName(roleDto.getName()) == null){
            return rolesRepository.save(roleMapper.map(roleDto)).getId();
        }
        else throw new IllegalArgumentException("Such role already exists");
    }

//    public void addRoleToUser(RoleDto roleDto){
//
//    }
//
//    public String deleteRole(RoleDto roleDto){
//        if (rolesRepository.findByName(roleDto.getName()) == null){
//            return rolesRepository.save(roleMapper.map(roleDto)).getId();
//        }
//        else throw new IllegalArgumentException("Such role already exists");
//    }
}
