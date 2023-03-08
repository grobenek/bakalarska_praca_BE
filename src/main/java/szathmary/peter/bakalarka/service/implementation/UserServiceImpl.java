package szathmary.peter.bakalarka.service.implementation;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import szathmary.peter.bakalarka.entity.User;
import szathmary.peter.bakalarka.exception.UserAlreadyRegisteredException;
import szathmary.peter.bakalarka.exception.UserNotFoundException;
import szathmary.peter.bakalarka.exception.WrongPasswordException;
import szathmary.peter.bakalarka.repository.UserRepository;
import szathmary.peter.bakalarka.service.UserService;

public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public List<User> findAll() {
    return this.userRepository.findAll();
  }

  @Override
  public User findByEmail(String email) {
    return this.userRepository.findByEmail(email);
  }

  @Override
  public User findByUsername(String username) {
    return this.userRepository.findByUsername(username);
  }

  @Override
  public User registerUser(User pUser) throws UserAlreadyRegisteredException {

    if (this.userRepository.findByEmail(pUser.getEmail()) == null) {
      throw new UserAlreadyRegisteredException(
          "User with email " + pUser.getEmail() + " is already registered");
    }

    User user = User.builder().username(pUser.getUsername()).firstName(pUser.getFirstName())
        .lastName(pUser.getLastName()).email(pUser.getEmail())
        .password(this.passwordEncoder.encode(pUser.getPassword())).build();

    return this.userRepository.save(user);
  }

  @Override
  public Boolean verifyUser(User user) throws UserNotFoundException {
    User userFromDatabase = this.userRepository.findByEmail(user.getEmail());

    if (userFromDatabase == null) {
      throw new UserNotFoundException("User with email " + user.getEmail() + " was not found");
    }

    return this.passwordEncoder.matches(user.getPassword(), userFromDatabase.getPassword());
  }

  @Override
  public User updateUserPassword(User user) throws UserNotFoundException, WrongPasswordException {
    User userFromDatabase = this.userRepository.findByEmail(user.getEmail());

    if (userFromDatabase == null) {
      throw new UserNotFoundException("User with email " + user.getEmail() + " was not found");
    }

    if (!this.verifyUser(user)) {
      throw new WrongPasswordException("Wrong password for user with login " + user.getUsername());
    }

    String newPassword = this.passwordEncoder.encode(user.getPassword());

    user.setPassword(newPassword);

    return this.userRepository.save(user);
  }

  @Override
  public User updateUser(User user) {
    return this.userRepository.save(user);
  }

  @Override
  public void removeUser(User user) {
    this.userRepository.delete(user);
  }
}
