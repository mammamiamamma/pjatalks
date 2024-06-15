package s26901.pjatalks.Controller.API;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import s26901.pjatalks.DTO.Input.RoleDto;
import s26901.pjatalks.Service.RolesService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RolesService rolesService;

    public RoleController(RolesService rolesService) {
        this.rolesService = rolesService;
    }
    @PostMapping
    public String addRole(@RequestBody RoleDto roleDto){
        return rolesService.saveRole(roleDto);
    }
}
