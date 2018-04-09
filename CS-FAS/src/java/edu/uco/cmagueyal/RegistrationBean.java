package edu.uco.cmagueyal;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import javax.sql.DataSource;

@Named(value = "registrationBean")
@SessionScoped
public class RegistrationBean implements Serializable {

    private String confirmationUserName;
    private String confirmationCode;
    private String userName;
    private String firstName;
    private String lastName;
    private String password;
    private String phone;
    private String ucoId;
    private String major;
    private String greeting;
    private String error;
    private boolean userNameOK;
    private boolean firstNameOK;
    private boolean lastNameOK;
    private boolean passwordOK;
    private boolean phoneOK;
    private boolean ucoOK;
    private boolean majorOK;
    private SendMailTls mail;

    // resource injection
    @Resource(name = "jdbc/ds_wsp")
    private DataSource ds;

    @PostConstruct
    public void init() {
        userNameOK = false;
        firstNameOK = false;
        lastNameOK = false;
        passwordOK = false;
        majorOK = false;
        ucoOK = false;
        phoneOK = false;
        mail = new SendMailTls();
    }

    public String register() {
        Pattern pattern = Pattern.compile("\\w+@uco.edu", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(userName);

        Pattern phonePattern = Pattern.compile("^?(\\d{3})?- ?(\\d{3})- ?(\\d{4})$");
        Matcher matcherPhone = phonePattern.matcher(phone);
        if (userName.trim().isEmpty()) {
            Utility.errorMessage("*Username Required", "regform:user");
            userNameOK = false;
        } else if (!matcher.matches()) {
            Utility.errorMessage("*Username must be UCO Email", "regform:user");
            userNameOK = false;
        } else {
            userNameOK = true;
        }
        if (firstName.trim().isEmpty()) {
            Utility.errorMessage("*First name Required", "regform:first");
            firstNameOK = false;
        } else {
            firstNameOK = true;
        }
        if (lastName.trim().isEmpty()) {
            Utility.errorMessage("*Lastname Required", "regform:last");
            lastNameOK = false;
        } else {
            lastNameOK = true;
        }
        if (password.trim().isEmpty()) {
            Utility.errorMessage("*Password Required", "regform:password");
            passwordOK = false;
        } else if (password.trim().length() < 3) {
            Utility.errorMessage("*Password must be at least 3 characters long",
                    "regform:password");
            passwordOK = false;
        } else {
            passwordOK = true;
        }
        if (ucoId.trim().isEmpty()) {
            Utility.errorMessage("*UCO ID Required", "regform:ucoId");
            ucoOK = false;
        } else if (ucoId.trim().length() != 8) {
            Utility.errorMessage("*UCO ID must be 8 digits",
                    "regform:ucoId");
            ucoOK = false;
        } else if (!ucoId.matches("[0-9]+")) {
            Utility.errorMessage("*UCO ID must be only digits",
                    "regform:ucoId");
            ucoOK = false;
        } else {
            ucoOK = true;
        }
        if (major.isEmpty()) {
            Utility.errorMessage("*Major code Required", "regform:major");
            majorOK = false;
        } else if (major.trim().length() != 4) {
            Utility.errorMessage("*Major code must be 4 digits long",
                    "regform:major");
            majorOK = false;
        } else if (!major.matches("[0-9]+")) {
            Utility.errorMessage("*Major code must be only digits",
                    "regform:major");
            majorOK = false;
        } else {
            majorOK = true;
        }
        if (phone.trim().isEmpty()) {
            Utility.errorMessage("*Phone Required", "regform:phone");
            phoneOK = false;

        } else if (!matcherPhone.matches()) {
            Utility.errorMessage("*Phone number format: 'xxx-xxx-xxxx'", "regform:phone");
            phoneOK = false;
        } else {
            phoneOK = true;
        }
        if (userNameOK && firstNameOK && lastNameOK && passwordOK && phoneOK
                && ucoOK && majorOK) {
            String addResult = "";
            try {
                addResult = addNewUser();
            } catch (SQLException ex) {
                Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            return addResult;
        }
        return "";
    }

    public String addNewUser() throws SQLException {
        String code;
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        try {
            PreparedStatement ps1 = conn.prepareStatement(
                    "select USERNAME from usertable where USERNAME = ? or UCO_ID = ?"
            );
            ps1.setString(1, userName);
            ps1.setString(2, ucoId);
            ResultSet result = ps1.executeQuery();

            List<String> usernameList = new ArrayList<>();
            while (result.next()) {
                String u = result.getString("USERNAME");
                usernameList.add(u);
            }
            if (usernameList.isEmpty()) {
                code = Utility.generateRandomCode();
                PreparedStatement ps = conn.prepareStatement(
                        "Insert into usertable (USERNAME, FIRSTNAME, LASTNAME, PASSWORD, UCO_ID, MAJOR, PHONE, CODE)"
                        + "Values(?,?,?,?,?,?,?,?)"
                );
                ps.setString(1, userName);
                ps.setString(2, firstName);
                ps.setString(3, lastName);
                ps.setString(4, Utility.encrypt(password));
                ps.setString(5, ucoId);
                ps.setString(6, major);
                ps.setString(7, phone);
                ps.setString(8, code);
                ps.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(
                        "Insert into grouptable (GROUPNAME, USERNAME) "
                        + "Values(?,?)"
                );
                //The new user's group is set to "PendingConfirm" to create an error when trying to login.
                //The user must confirm their account to have this changed to "studentgroup
                ps2.setString(1, "PendingConfirm");
                ps2.setString(2, userName);
                ps2.executeUpdate();

                PreparedStatement ps3 = conn.prepareStatement(
                        "Insert into filestorage(FILE_NAME,"
                        + " FILE_TYPE, FILE_SIZE, FILE_CONTENTS, USERNAME) "
                        + "values(null,null,null, null ,?)"
                );
                ps3.setString(1, userName);
                ps3.executeUpdate();

                sendEmail(code);
                return "successful";

            } else if (!usernameList.isEmpty()) {
                Utility.errorMessage("**Error: Username or UCO ID already in use.", "regform:registerError");
            } else {
                Utility.errorMessage("**Error: Try again or contact CS faculty advisor for further help.", "regform:registerError");
            }
        } finally {
            conn.close();
        }
        return "register";
    }

    public String confirm() throws SQLException, InterruptedException {
        String userCode = "";
        if (confirmationUserName.isEmpty()) {
            Utility.errorMessage("*Please enter a username", "confirmform:username");
        }
        if (confirmationCode.isEmpty()) {
            Utility.errorMessage("*Please enter a confirmation code", "confirmform:code");
        } else {
            Connection conn = ds.getConnection();
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "select CODE from usertable where USERNAME = ?");
                ps.setString(1, confirmationUserName);
                ResultSet result = ps.executeQuery();
                while (result.next()) {
                    userCode = result.getString("CODE");
                }
                if (userCode.equals(confirmationCode)) {
                    updateGroup(confirmationUserName, "studentgroup");
                    confirmationUserName = confirmationCode = "";
                } else {
                    Utility.errorMessage("*Code entered is invalid. Try again.", "confirmform:hidden");

                    return "";
                }
            } finally {
                conn.close();
            }
        }
        return "";
    }

    private void updateGroup(String username, String group) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "update GROUPTABLE set GROUPNAME = ? where USERNAME = ?;"
            );
            ps.setString(1, group);
            ps.setString(2, username);
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Confirmation successful! You may log in now.", "confirmform:hidden");
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public String getfirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String name) {
        this.lastName = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUcoId() {
        return ucoId;
    }

    public void setUcoId(String id) {
        this.ucoId = id;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getGreeting() {
        return greeting;
    }

    public String getError() {
        return error;
    }

    public String getConfirmationUserName() {
        return confirmationUserName;
    }

    public void setConfirmationUserName(String confirmationUserName) {
        this.confirmationUserName = confirmationUserName;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public void sendEmail(String code) {
        mail.registerEmail(this.firstName, this.userName, code);
    }

}
