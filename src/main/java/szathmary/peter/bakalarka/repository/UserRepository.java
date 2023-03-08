package szathmary.peter.bakalarka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import szathmary.peter.bakalarka.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

  User findByEmail(String email);

  User findByUsername(String username);
}