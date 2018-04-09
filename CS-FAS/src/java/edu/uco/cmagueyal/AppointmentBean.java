package edu.uco.cmagueyal;

import java.io.Serializable;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.sql.DataSource;
import org.primefaces.event.SelectEvent;

@Named(value = "AppointmentBean")
@SessionScoped

public class AppointmentBean implements Serializable {

    @Resource(name = "jdbc/ds_wsp")
    private DataSource ds;
    private Date date;
    @Temporal(TemporalType.TIME)
    private Date time;
    private String username;
    private User student;
    private SendMailTls mail;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private List<Appointment> appointments;
    private List<Appointment> myAppointments;
    private List<StudentCourse> studentCompleted;
    private List<StudentCourse> studentFuture;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Principal p = fc.getExternalContext().getUserPrincipal();
        username = p.getName();
        try {
            appointments = loadAvailable();

        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            myAppointments = loadMyAppointments();
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        mail = new SendMailTls();
    }

    public void onDateSelect(SelectEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SimpleDateFormat myFormat = new SimpleDateFormat("YYYY/MM/DD");
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Date Selected", myFormat.format(event.getObject())));
    }

    public String create(String lastName) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        if (date == null || time == null) {
            Utility.errorMessage("Appointment Date/Time invalid", "slotsform:datetime");
        } else {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "insert into appointments (advisor, advisor_last, app_date, app_time, student)"
                        + " values (?, ?, ?, ?, ?)"
                );
                ps.setString(1, username);
                ps.setObject(2, lastName);
                ps.setObject(3, date);
                ps.setObject(4, timeFormat.format(time));
                ps.setString(5, null);
                ps.executeUpdate();
            } finally {
                conn.close();
            }
            Utility.infoMessage("Appointment Created!", "slotsform:datetime");
        }
        appointments = loadAvailable(); // reload the updated info
        return "manageslots";
    }

    public String advisementComplete(Appointment app) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "delete from appointments where ID = ?"
            );
            ps.setLong(1, app.getId());
            ps.executeUpdate();
            PreparedStatement ps2 = conn.prepareStatement(
                    "update usertable set ADVISED = ? where USERNAME = ?"
            );
            ps2.setBoolean(1, true);
            ps2.setString(2, app.getStudent());
            ps2.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Student Advisement Completed", "");
        myAppointments = loadMyAppointments();
        return "myappointments"; // re-display page
    }

    public String cancel(Appointment app) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        mail.advisorCancelledEmail(app);
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "delete from appointments where ID = " + app.getId() + ""
            );
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Cancel Successful. Student was notified of cancellation.", "successful");
        myAppointments = loadMyAppointments();
        return "myappointments"; // re-display page
    }

    public String delete(Integer id) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "delete from appointments where ID = " + id + ""
            );
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        Utility.infoMessage("Time Slot Deleted!", "success");
        appointments = loadAvailable(); // reload the updated info
        return "manageslots"; // re-display page
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
                    + "from appointments where advisor = ? and student is null"
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

    public List<Appointment> loadMyAppointments() throws SQLException {
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
                    + "from appointments where advisor = ? and student is not null"
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

    public String viewProfile(String studentUserName) {
        try {
            this.student = setStudent(studentUserName);
        } catch (SQLException ex) {
            Logger.getLogger(AppointmentBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            setStudentCompleted();
        } catch (SQLException ex) {
            Logger.getLogger(AppointmentBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            setStudentFuture();
        } catch (SQLException ex) {
            Logger.getLogger(AppointmentBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "viewstudent";
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date1) {
        this.date = date1;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date date2) {
        this.time = date2;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String viewTimeSlots() {
        try {
            appointments = loadAvailable();

        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "manageslots";
    }

    public String viewAppointments() {
        try {
            myAppointments = loadMyAppointments();
        } catch (SQLException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "myappointments";
    }

    public List<StudentCourse> getStudentCompleted() {
        return studentCompleted;
    }

    public void setStudentCompleted() throws SQLException {
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
                    + "SC.`crn` = c.`crn` WHERE SC.`studentid` = " + student.getUserID() + " and SC.completed = 1 "
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                StudentCourse c = new StudentCourse();
                c.setStudentCourseID(student.getUserID());
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
        this.studentCompleted = list;
    }

    public List<StudentCourse> getStudentFuture() {
        return studentFuture;
    }

    public void setStudentFuture() throws SQLException {
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
                    + "SC.`crn` = c.`crn` WHERE SC.`studentid` = " + student.getUserID() + " and SC.completed = 0 "
            );
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                StudentCourse c = new StudentCourse();
                c.setStudentCourseID(student.getUserID());
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
        this.studentFuture = list;
    }

    public User getStudent() {
        return student;
    }

    public User setStudent(String student) throws SQLException {
        Connection conn = ds.getConnection();
        if (conn == null) {
            throw new SQLException("conn is null; Can't get db connection");
        }
        User s = new User();
        if (student != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT usertable.ID, usertable.USERNAME, usertable.FIRSTNAME,"
                        + " usertable.LASTNAME, usertable.MAJOR, usertable.PASSWORD, "
                        + "usertable.UCO_ID, usertable.PHONE, grouptable.GROUPNAME "
                        + "FROM usertable INNER JOIN grouptable ON "
                        + "usertable.USERNAME = grouptable.USERNAME WHERE "
                        + "usertable.USERNAME = '" + student + "'"
                );
                ResultSet result = ps.executeQuery();
                while (result.next()) {
                    s.setUserID(result.getInt("usertable.ID"));
                    s.setUserName(result.getString("usertable.USERNAME"));
                    s.setFirstName(result.getString("usertable.FIRSTNAME"));
                    s.setLastName(result.getString("usertable.LASTNAME"));
                    s.setPhone(result.getString("usertable.PHONE"));
                    s.setPassword(result.getString("usertable.PASSWORD"));
                    s.setUcoID(result.getString("usertable.UCO_ID"));
                    s.setMajor(result.getString("usertable.MAJOR"));
                    s.setGroup(result.getString("grouptable.GROUPNAME"));
                }
            } finally {
                conn.close();
            }
        }
        return s;
    }
}
