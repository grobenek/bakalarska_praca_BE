package szathmary.peter.bakalarka.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity(name = "user")
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @NotBlank(message = "Username cannot be blank")
  @NotNull(message = "Username cannot be null")
  @Column(name = "username", nullable = false, unique = true)
  private String username;

  @NotBlank(message = "Password cannot be blank")
  @NotNull(message = "Password cannot be null")
  @Column(name = "password", nullable = false)
  private String password;

  @NotBlank(message = "First name cannot be blank")
  @NotNull(message = "Last name cannot be null")
  @Column(name = "first_name", nullable = false)
  private String firstname;

  @NotBlank(message = "Last name cannot be blank")
  @NotNull(message = "Last name cannot be null")
  @Column(name = "last_name", nullable = false)
  private String lastname;

  @NotBlank(message = "Email cannot be blank")
  @NotNull(message = "Email cannot be null")
  @Email(message = "Email is not in right format")
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Builder

  public User(String username, String password, String firstName, String lastName, String email) {
    this.username = username;
    this.password = password;
    this.firstname = firstName;
    this.lastname = lastName;
    this.email = email;
  }
}
