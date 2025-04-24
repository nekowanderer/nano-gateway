package idv.clu.gateway.iam.dto;

import java.util.Objects;

/**
 * @author clu
 */
public class UserDTO {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(username, userDTO.username)
                && Objects.equals(password, userDTO.password)
                && Objects.equals(firstName, userDTO.firstName)
                && Objects.equals(lastName, userDTO.lastName)
                && Objects.equals(email, userDTO.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, firstName, lastName, email);
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
