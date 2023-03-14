package szathmary.peter.bakalarka.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginInformationDto {
  @NotBlank(message = "username cannot be blank")
  @NotNull(message = "username cannot be null")
  private String username;
  @NotBlank(message = "password cannot be blank")
  @NotNull(message = "password cannot be null")
  private String password;
}
