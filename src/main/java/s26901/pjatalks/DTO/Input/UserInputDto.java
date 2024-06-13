package s26901.pjatalks.DTO.Input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public class UserInputDto {
    @NotBlank(message = "Username is mandatory")
    @Length(min = 1, max = 30, message = "Username must be between 1 and 30 characters")
    private String username;
    @NotBlank(message = "Password is mandatory")
    @Length(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;
    @NotBlank(message = "Email address is mandatory")
    @Email(message = "Invalid email format")
    private String emailAddress;
    @Length(max = 330, message = "Short bio must be less than 330 characters")
    private String shortBio;
    private Set<RoleDto> userRoles;

    public UserInputDto() {
    }

    public UserInputDto(String username, String password, String emailAddress, String shortBio, Set<RoleDto> userRoles) {
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.shortBio = shortBio;
        this.userRoles = userRoles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
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
}
