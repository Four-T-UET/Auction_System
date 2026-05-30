package auction.logic.model;

import auction.logic.enums.AuthenticationException;
import java.io.Serializable;
import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class User extends Entity implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(User.class.getName());
    private String username;
    private String password;
    private String role;
    public User( String username, String password, String role){
        super();
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public String getUsername(){
        return username;
    }
    public synchronized boolean login(String name, String pass){
        boolean check = true;
        try{
            check = (this.username.equals(name)) && (this.password.equals(pass));
            if(check == false){
                throw new AuthenticationException("Tài khoản đăng nhập không hợp lệ");
            }
        }catch (AuthenticationException e){
            LOGGER.log(Level.WARNING, e.getMessage(), e); // catch này sau này UI xử lý
        }
        return check;
    }
    // synchronized để tránh 2 luồng cùng login cùng lúc
}

