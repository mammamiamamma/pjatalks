package s26901.pjatalks.DTO.Input;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public class LoginInput {
    @NotBlank(message = "Username must not be empty")
    @Length(min = 1, max = 30, message = "Username must be between 1 and 30 characters")
    private String username;
    @NotBlank(message = "Password must not be empty")
    @Length(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;

    public LoginInput() {
    }

    public LoginInput(String username, String password) {
        this.username = username;
        this.password = password;
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
}
