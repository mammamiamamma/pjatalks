package s26901.pjatalks.Entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

@Document(collection = "users")
@TypeAlias("User")
public class User implements UserDetails {
    @Id
    private String id;
    @NotBlank(message = "Username is mandatory")
    @Length(min = 1, max = 30, message = "Username must be between 1 and 30 characters")
    //if size is validated in inputDto, should it validate here as well or maybe validate the format?
    private String username;
    @NotBlank(message = "Password is mandatory")
    @Length(min = 8, max = 30, message = "Password must be between 8 and 20 characters")
    private String password;
    @Indexed(unique = true)
    @NotBlank(message = "Email address is mandatory")
    @Email(message = "Invalid email format")
    private String emailAddress;
    @DBRef
    @Length(max = 330, message = "Short bio must be less than 330 characters")
    private String shortBio;
    @DBRef
    private Set<UserRole> roles;
    @LastModifiedDate
    private Date lastCheckedNotifications;

    public User() {
    }

    public User(String id, String username, String password, String emailAddress, String shortBio, Set<UserRole> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.shortBio = shortBio;
        this.roles = roles;
    }

    public User(String id, String username, String password, String emailAddress, String shortBio) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.shortBio = shortBio;
    }

    public User(String username, String password, String emailAddress, String shortBio, Set<UserRole> roles) {
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.shortBio = shortBio;
        this.roles = roles;
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
    public String getPassword() {
        return password;
    }
    public void setPassword(String password){
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
    @Override
    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
//        return UserDetails.super.isAccountNonLocked();
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
//        return UserDetails.super.isCredentialsNonExpired();
        return true;
    }

    @Override
    public boolean isEnabled() {
//        return UserDetails.super.isEnabled();
        return true;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void addRole(UserRole userRole){
        roles.add(userRole);
    }

    public void deleteRole(UserRole userRole){
        roles.remove(userRole);
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public Date getLastCheckedNotifications() {
        return lastCheckedNotifications;
    }

    public void setLastCheckedNotifications(Date lastCheckedNotifications) {
        this.lastCheckedNotifications = lastCheckedNotifications;
    }
}
