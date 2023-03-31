package szathmary.peter.bakalarka.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import szathmary.peter.bakalarka.exception.InvalidElectricQuantityException;
import szathmary.peter.bakalarka.exception.NoDataFound;
import szathmary.peter.bakalarka.exception.UserAlreadyRegisteredException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyRegisteredException.class)
  public ResponseEntity<String> handleUserAlreadyRegisteredException(UserAlreadyRegisteredException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
  }

  @ExceptionHandler(NoDataFound.class)
  public ResponseEntity<String> handleNoDataFoundException(NoDataFound ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(InvalidElectricQuantityException.class)
  public ResponseEntity<String> handleInvalidElectricQuantityException(InvalidElectricQuantityException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

}
