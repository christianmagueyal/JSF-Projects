package edu.uco.cmagueyal;

import java.util.Date;

public class Appointment {

    private long id;
    private String advisor;
    private Date date;
    private Date time;
    private String student;
    private String advisorLastName;

    public String getAdvisorLastName() {
        return advisorLastName;
    }

    public void setAdvisorLastName(String advisorLastName) {
        this.advisorLastName = advisorLastName;
    }

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
