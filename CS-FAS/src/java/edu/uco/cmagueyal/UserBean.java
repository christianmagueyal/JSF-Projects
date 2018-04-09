package edu.uco.cmagueyal;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;
import javax.validation.constraints.Size;

@Named(value = "UserBean")
@SessionScoped
public class UserBean implements Serializable {

    // resource injection
    @Resource(name = "jdbc/ds_wsp")
    private DataSource ds;

    private SendMailTls mail;
    private List<User> users;
    private List<StudentCourse> completed;
    private List<StudentCourse> futureCourses;
    private User currentUser;
    private boolean edit;
    private String crn;
    private String username;
    private String oldPassword;
    @Size(min = 3, message = "*New Password must be at least 3 characters.")
    private String newPassword;
    private List<Appointment> appointments;
    private List<Appointment> myAppointments;


    @PostConstruct
    public void init() {
        try {
            users = loadUsers();
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            appointments = (loadAvailable());
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            myAppointments = (loadAppointments());
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Principal p = fc.getExternalContext().getUserPrincipal();
        username = p.getName();
        try {
            currentUser = setCurrentUser(username);
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            loadFutureCourses();
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        edit = false;
        mail = new SendMailTls();
    }

    public String getUsername() {
        return username;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<User> loadUsers() throws SQLException {
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        List<User> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT usertable.ID, usertable.USERNAME, usertable.FIRSTNAME,"
                    + " usertable.LASTNAME, usertable.MAJOR, usertable.PASSWORD, "
                    + "usertable.UCO_ID, usertable.PHONE, grouptable.GROUPNAME "
                    + "FROM usertable INNER JOIN grouptable ON "
                    + "usertable.USERNAME = grouptable.USERNAME WHERE "
                    + "grouptable.GROUPNAME = 'studentgroup'"
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                User c = new User();
                c.setUserID(result.getInt("usertable.ID"));
                c.setUserName(result.getString("usertable.USERNAME"));
                c.setFirstName(result.getString("usertable.FIRSTNAME"));
                c.setLastName(result.getString("usertable.LASTNAME"));
                c.setPhone(result.getString("usertable.PHONE"));
                c.setPassword(result.getString("usertable.PASSWORD"));
                c.setUcoID(result.getString("usertable.UCO_ID"));
                c.setMajor(result.getString("usertable.MAJOR"));
                c.setGroup(result.getString("grouptable.GROUPNAME"));
                list.add(c);
            }
        } finally {
            conn.close();
        }
        return list;
    }

    public void loadStudentComplete() throws SQLException {
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        List<StudentCourse> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT coursename, courseprefix, coursenumber, C.`crn`, studentid from "
                    + "studentcourse AS SC INNER JOIN coursetable AS C ON "
                    + "SC.`crn` = c.`crn` WHERE SC.`studentid` = " + currentUser.getUserID() + " and SC.completed = 1 "
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                StudentCourse c = new StudentCourse();
                c.setStudentCourseID(currentUser.getUserID());
                c.setCourseName(result.getString("COURSENAME"));
                c.setCoursePrefix(result.getString("COURSEPREFIX"));
                c.setCourseNumber(result.getString("COURSENUMBER"));
                c.setCrn(result.getString("CRN"));
                c.setStudentID(result.getInt("STUDENTID"));
                list.add(c);
            }
        } finally {
            conn.close();
        }
        completed = list;
    }

    public String myCourses() throws SQLException {
        loadStudentComplete();
        loadFutureCourses();
        return "courses";
    }

    public void loadFutureCourses() throws SQLException {
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        List<StudentCourse> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT coursename, courseprefix, coursenumber, C.`crn`, studentid from "
                    + "studentcourse AS SC INNER JOIN coursetable AS C ON "
                    + "SC.`crn` = c.`crn` WHERE SC.`studentid` = " + currentUser.getUserID() + " and SC.completed = 0 "
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                StudentCourse c = new StudentCourse();
                c.setStudentCourseID(currentUser.getUserID());
                c.setCourseName(result.getString("COURSENAME"));
                c.setCoursePrefix(result.getString("COURSEPREFIX"));
                c.setCourseNumber(result.getString("COURSENUMBER"));
                c.setCrn(result.getString("CRN"));
                c.setStudentID(result.getInt("STUDENTID"));
                list.add(c);
            }
        } finally {
            conn.close();
        }
        setFutureCourses(list);
    }

    public List<Appointment> loadAvailable() throws SQLException {

        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        List<Appointment> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "select advisor, advisor_last, id, app_date, app_time, student "
                    + "from appointments where student is null"
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                Appointment a = new Appointment();
                a.setAdvisor(result.getString("advisor"));
                a.setAdvisorLastName(result.getString("advisor_last"));
                a.setDate(result.getTimestamp("app_date"));
                a.setTime(result.getTimestamp("app_time"));
                a.setStudent(result.getString("student"));
                a.setId(result.getInt("id"));
                list.add(a);
            }
        } finally {
            conn.close();
        }
        return list;
    }

    public List<Appointment> loadAppointments() throws SQLException {
        if (ds == null) {
            throw new SQLException("ds is null; Can't get data source");
        }
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        List<Appointment> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "select advisor, advisor_last, id, app_date, app_time, student "
                    + "from appointments where student = ?"
            );
            ps.setString(1, username);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                Appointment a = new Appointment();
                a.setAdvisor(result.getString("advisor"));
                a.setAdvisorLastName(result.getString("advisor_last"));
                a.setDate(result.getTimestamp("app_date"));
                a.setTime(result.getTimestamp("app_time"));
                a.setStudent(result.getString("student"));
                a.setId(result.getInt("id"));
                list.add(a);
            }
        } finally {
            conn.close();
        }
        return list;
    }

    public boolean hasPicture() throws SQLException {
        Connection conn = ds.getConnection();
        List<String> f = new ArrayList<>();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "select FILE_NAME from filestorage where USERNAME = ? and FILE_NAME is not null"
            );
            ps.setString(1, username);
            ResultSet result = ps.executeQuery();

            while (result.next()) {
                f.add(result.getString("FILE_NAME"));
            }
        } finally {
            conn.close();
        }
        return f.size() > 0;
    }

    public User setCurrentUser(String username) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        User c = new User();
        if (username != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT usertable.ID, usertable.USERNAME, usertable.FIRSTNAME,"
                        + " usertable.LASTNAME, usertable.MAJOR, usertable.PASSWORD, "
                        + "usertable.UCO_ID, usertable.PHONE, grouptable.GROUPNAME "
                        + "FROM usertable INNER JOIN grouptable ON "
                        + "usertable.USERNAME = grouptable.USERNAME WHERE "
                        + "usertable.USERNAME = ?"
                );
                ps.setString(1, username);
                ResultSet result = ps.executeQuery();
                while (result.next()) {
                    c.setUserID(result.getInt("usertable.ID"));
                    c.setUserName(result.getString("usertable.USERNAME"));
                    c.setFirstName(result.getString("usertable.FIRSTNAME"));
                    c.setLastName(result.getString("usertable.LASTNAME"));
                    c.setPhone(result.getString("usertable.PHONE"));
                    c.setPassword(result.getString("usertable.PASSWORD"));
                    c.setUcoID(result.getString("usertable.UCO_ID"));
                    c.setMajor(result.getString("usertable.MAJOR"));
                    c.setGroup(result.getString("grouptable.GROUPNAME"));
                }
            } finally {
                conn.close();
            }
        }
        return c;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean getEdit() {
        return this.edit;
    }

    public void setEdit(boolean b) {
        this.edit = b;
    }

    public String editUser(User user) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "update USERTABLE set USERNAME= ?, FIRSTNAME=?, LASTNAME=?,"
                    + " UCO_ID=?, PHONE=?, MAJOR= ? where ID=?;"
            );
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setLong(4, Long.parseLong(user.getUcoID()));
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getMajor());
            ps.setLong(7, user.getUserID());
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Successfully Edited: " + user.getUserName(), "");
        // reloadAll() reloads user info for admin pages
        reloadAll();
        if (edit) {
            edit = !edit;
        }
        return null;
    }

    public String signUp(Appointment app) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        mail.appointmentSignUpEmail(app, currentUser);
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "update appointments set student = ? where ID = ?"
            );
            ps.setString(1, username);
            ps.setLong(2, app.getId());
            ps.executeUpdate();
            
        } finally {
            conn.close();
        }
        Utility.infoMessage("Sign Up: Successful", null);
        appointments = loadAvailable(); // reload open appointments
        myAppointments = loadAppointments(); // reload myAppointments signed up appointmens
        return null;
    }

    public String cancel(Appointment app) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        mail.studentCancelledEmail(app.getDate(), app.getTime(), app.getAdvisor());
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "update appointments set student = null where ID = ?"
            );
            ps.setLong(1, app.getId());
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Appointment Canceled", null);
        appointments = loadAvailable(); // reload open appointments
        myAppointments = loadAppointments(); // reload myAppointments signed up appointmens
        return null; // re-display page
    }

    public String deleteUser(User student) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "delete from studentcourse where studentid = ?"
            );
            ps.setLong(1, student.getUserID());
            ps.executeUpdate();
            PreparedStatement ps2 = conn.prepareStatement(
                    "update appointments set student = null where student = ?"
            );
            ps2.setString(1, student.getUserName());
            ps2.executeUpdate();
            
            PreparedStatement ps3 = conn.prepareStatement(
                    "delete from usertable where ID = ?"
            );
            ps3.setLong(1, student.getUserID());
            ps3.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Deletion Successful", "accountsform:success");
        users = loadUsers(); // reload the updated info
        return null; // re-display page
    }

    public String resetPassword(User user) throws SQLException {
        String tempPassword = Utility.generateRandomCode();
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        mail.passwordResetEmail(user.getFirstName(), tempPassword, user.getUserName());
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "update USERTABLE set PASSWORD=? where ID=?"
            );

            ps.setString(1, Utility.encrypt(tempPassword));
            ps.setLong(2, user.getUserID());
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Password Reset Successful. New password sent to student's UCO email.", "accountsform:success");
        users = loadUsers(); // reload the updated info
        return null;
    }

    public String changePassword() throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        if (Utility.encrypt(oldPassword).equals(currentUser.getPassword())) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "update USERTABLE set PASSWORD=? where ID=?"
                );
                ps.setString(1, Utility.encrypt(newPassword));
                ps.setLong(2, currentUser.getUserID());
                ps.executeUpdate();
            } finally {
                conn.close();
            }
            Utility.infoMessage("Password change successful!", "passwordform:success");
        } else {
            Utility.errorMessage("*Old Password doesn't match. Try again.", "passwordform:old");
        }
        return "";
    }

    public String addCourse() throws SQLException {
        String resultCRN = "";
        if (crn.isEmpty()) {
            Utility.errorMessage("ERROR: No CRN entered", "mycourseform:success");
        } else {
            Connection conn = ds.getConnection();
            if (conn == null) {
                throw new SQLException("conn is null; Can't get db connection");
            }
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "select CRN from coursetable where CRN = ?"
                );
                ps.setString(1, crn);
                ResultSet result = ps.executeQuery();
                while (result.next()) {
                    resultCRN = result.getString("CRN");
                }
                if (!resultCRN.isEmpty()) {
                    PreparedStatement ps2 = conn.prepareStatement(
                            "insert into studentcourse (STUDENTID, CRN) "
                            + "values (?,?)"
                    );
                    ps2.setLong(1, currentUser.getUserID());
                    ps2.setString(2, resultCRN);
                    ps2.executeUpdate();
                }
            } finally {
                conn.close();
            }
            if (resultCRN.isEmpty()) {
                Utility.errorMessage("ERROR: CRN entered is invalid", "");
            } else {
                loadFutureCourses();

                Utility.infoMessage("Course Successfully Added!", "mycourseform:success");
            }
        }
        crn = "";
        return "courses";
    }

    public String reloadAll() {
        try {
            users = loadUsers();
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return "managestudents";
    }

    public void refreshMyAppointments(){
        try {
            this.myAppointments = loadAppointments();
            loadFutureCourses();
            loadStudentComplete();
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public List<StudentCourse> getCompleted() {
        return completed;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<Appointment> getMyAppointments() {
        
        return myAppointments;
    }

    public void setMyAppointments(List<Appointment> myAppointments) {
        this.myAppointments = myAppointments;
    }

    public List<StudentCourse> getFutureCourses() {
        return futureCourses;
    }

    public void setFutureCourses(List<StudentCourse> futureCourses) {
        this.futureCourses = futureCourses;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }
}
