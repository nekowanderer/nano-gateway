package idv.clu.gateway.iam.dto;

/**
 * @author clu
 */
public record UserDTO(
        String username,
        String password,
        String firstName,
        String lastName,
        String email
) {

}
