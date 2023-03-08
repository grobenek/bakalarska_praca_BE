package szathmary.peter.bakalarka.exception;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UserAlreadyRegisteredException extends Throwable {

  public UserAlreadyRegisteredException(String message) {
    super(message);
  }
}
