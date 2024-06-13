package s26901.pjatalks.DTO.Output;

import s26901.pjatalks.DTO.Input.RoleDto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class UserOutputDto {
    private String id;
    private String username;
    private String shortBio;
    private Date LastCheckedNotifications; //up for removal
    private Set<RoleDto> userRoles;

    public UserOutputDto() {
    }

    public UserOutputDto(String id, String username, String shortBio, Date lastCheckedNotifications, Set<RoleDto> userRoles) {
        this.id = id;
        this.username = username;
        this.shortBio = shortBio;
        this.LastCheckedNotifications = lastCheckedNotifications;
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

    public Date getLastCheckedNotifications() {
        return LastCheckedNotifications;
    }

    public void setLastCheckedNotifications(Date lastCheckedNotifications) {
        LastCheckedNotifications = lastCheckedNotifications;
    }
    public Set<String> getRoleNames() {
        return userRoles.stream()
                .map(RoleDto::getName) // Assuming RoleDto has a getName() method
                .collect(Collectors.toSet());
    }
}
