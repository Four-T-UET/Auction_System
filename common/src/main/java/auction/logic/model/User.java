package auction.logic.model;

import auction.logic.enums.AuthenticationException;
import javax.swing.*;

public abstract class User extends Entity {
    private String username;
    private String password;
    public User( String username, String password){
        super();
        this.username = username;
        this.password = password;
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
            System.out.println(e.getMessage()); // catch này sau này UI xử lý
        }
        return check;
    }
    // synchronized để avoid 2 thằng cùng login 1 thời điểm
}

