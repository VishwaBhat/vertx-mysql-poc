package io.vertx.starter.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel {
  private String email;
  private String name;
  private boolean contactVisibility;
  private String profilePic;
  private String password;

  public static String SCHEMA = "USERS";
}
