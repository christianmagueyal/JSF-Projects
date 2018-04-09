package edu.uco.cmagueyal;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class User {

    private long userID;
    
    @Size(min=1, message="Required")
    @Pattern(regexp="\\w+@uco.edu", message="Username must be UCO email")
    private String userName;
    
    @Size(min=1, message="Required")
    private String firstName;
    
    @Size(min=1, message="Required")
    private String lastName;
    
    @Size(min = 3, message="password must be min 3 characters")
    private String password;
    
    @Size(min=1, message="Required")
    @Pattern(regexp="^\\(?(\\d{3})\\)?- ?(\\d{3})- ?(\\d{4})$", message="Phone must look like '123-123-1234'")
    private String phone;

    private String group;
    
    @Size(min=8, max=8, message="UCO ID must be 8 digits")
    @Pattern(regexp="[0-9]+", message="UCO ID is digits only")
    private String ucoID;
    
    @Size(min=4, max=4, message="Major code must be 4 digits")
    @Pattern(regexp="[0-9]+", message=" Major code is digits only")
    private String major;

    private boolean edit;
    
    public boolean isEdit(){
        return edit;
    }
    public void setEdit(boolean edit){
        this.edit=edit;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }
    public String getUcoID() {
        return ucoID;
    }

    public void setUcoID(String ucoID) {
        this.ucoID = ucoID;
    }
    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

