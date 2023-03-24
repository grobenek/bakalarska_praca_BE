package szathmary.peter.bakalarka.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDtoWithPassword {
  @NotNull
  @NotBlank
  private String password;
  @NotNull
  @NotBlank
  private String username;
  @NotNull
  @NotBlank
  private String firstname;
  @NotNull
  @NotBlank
  private String lastname;
  @NotNull
  @NotBlank
  @Email
  private String email;
}
