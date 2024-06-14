package s26901.pjatalks.DTO.Output;

import s26901.pjatalks.DTO.Input.RoleDto;

import java.util.Set;
import java.util.stream.Collectors;

public class UserOutputDto {
    private String id;
    private String username;
    private String shortBio;
    private Set<RoleDto> userRoles;

    public UserOutputDto() {
    }

    public UserOutputDto(String id, String username, String shortBio, Set<RoleDto> userRoles) {
        this.id = id;
        this.username = username;
        this.shortBio = shortBio;
        this.userRoles = userRoles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getShortBio() {
        return shortBio;
    }

    public void setShortBio(String shortBio) {
        this.shortBio = shortBio;
    }

    public Set<RoleDto> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<RoleDto> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<String> getRoleNames() {
        return userRoles.stream()
                .map(RoleDto::getName) // Assuming RoleDto has a getName() method
                .collect(Collectors.toSet());
    }
}
