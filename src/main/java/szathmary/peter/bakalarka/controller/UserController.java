package szathmary.peter.bakalarka.controller;

import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import szathmary.peter.bakalarka.dto.UserDto;
import szathmary.peter.bakalarka.dto.UserDtoWithPassword;
import szathmary.peter.bakalarka.dto.UserLoginInformationDto;
import szathmary.peter.bakalarka.entity.User;
import szathmary.peter.bakalarka.exception.UserAlreadyRegisteredException;
import szathmary.peter.bakalarka.exception.UserNotFoundException;
import szathmary.peter.bakalarka.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/user")
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class UserController {

  private final UserService userService;
  private final ModelMapper modelMapper;

  public UserController(UserService userService) {
    this.userService = userService;
    this.modelMapper = new ModelMapper();
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<UserDto>> getAllUsers() {
    log.info("All users requested in UserController");

    List<User> userList = this.userService.findAll();

    List<UserDto> userDtos = userList.stream()
        .map(user -> this.modelMapper.map(user, UserDto.class)).toList();

    log.info("{} users returned in UserController", userDtos.size());
    return ResponseEntity.ok().body(userDtos);
  }

  @RequestMapping(value = "/register", method = RequestMethod.POST)
  public ResponseEntity<UserDto> registerUser(@RequestBody @Valid UserDtoWithPassword user)
      throws UserAlreadyRegisteredException {
    log.info("Register user with login {} requested in UserController", user.getUsername());

    log.info(user.toString());

    User userToCheck = this.modelMapper.map(user, User.class);

    this.userService.registerUser(userToCheck);

    log.info("User with login {} registered in UserController", userToCheck.getUsername());
    return ResponseEntity.ok().body(this.modelMapper.map(user, UserDto.class));
  }

  @RequestMapping(value = "/verify", method = RequestMethod.POST)
  public ResponseEntity<Boolean> verifyUser(@RequestBody @Valid UserLoginInformationDto user) {
    log.info("Verification of user with login {} requested in UserController", user.getUsername());

    User userToCheck = this.modelMapper.map(user, User.class);

    try {
      if (this.userService.verifyUser(userToCheck)) {
        log.info("User with login {} has been successfully verified", user.getUsername());
        return ResponseEntity.ok().body(true);
      } else {
        log.info("User with login {} has not been successfully verified", user.getUsername());
        return ResponseEntity.ok().body(false);
      }
    } catch (UserNotFoundException exception) {
      log.info("User with login {} was not found", user.getUsername());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
  }
}
