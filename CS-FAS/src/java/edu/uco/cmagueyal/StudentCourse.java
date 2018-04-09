package edu.uco.cmagueyal;

public class StudentCourse {

    private long studentCourseID;
    private String courseName;
    private String coursePrefix;
    private String courseNumber;
    private String crn;
    private long studentID;

    public long getStudentCourseID() {
        return studentCourseID;
    }

    public void setStudentCourseID(long studentCourseID) {
        this.studentCourseID = studentCourseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCoursePrefix() {
        return coursePrefix;
    }

    public void setCoursePrefix(String coursePrefix) {
        this.coursePrefix = coursePrefix;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public long getStudentID() {
        return studentID;
    }

    public void setStudentID(long studentID) {
        this.studentID = studentID;
    }
}
