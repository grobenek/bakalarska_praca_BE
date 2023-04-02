package szathmary.peter.bakalarka.service;

import szathmary.peter.bakalarka.entity.User;
import szathmary.peter.bakalarka.exception.UserAlreadyRegisteredException;
import szathmary.peter.bakalarka.exception.UserNotFoundException;
import szathmary.peter.bakalarka.exception.WrongPasswordException;

import java.util.List;

public interface UserService {

  List<User> findAll();

  User findByEmail(String email);

  User findByUsername(String username);

  User registerUser(User user) throws UserAlreadyRegisteredException;

  Boolean verifyUser(User user) throws UserNotFoundException;

  User updateUserPassword(User user) throws UserNotFoundException, WrongPasswordException;

  User updateUser(User user);

  void removeUser(User user);
}
