package auction.logic.RequestDTO;

import java.io.Serializable;

public class RegisterDTO implements Serializable {
  private String username;
  private String password;

  public RegisterDTO(String username, String password){
    this.username = username;
    this.password = password;
  }

  public String getUsername(){
    return this.username;
  }

  public String getPassword(){
    return this.password;
  }


}
