package org.example.model;



import org.bson.types.ObjectId;

public class Enrollment {
    private ObjectId id;

    // Embedded
    private Student student;
    private Course course;

    // Referenced
    private ObjectId studentId;
    private ObjectId courseId;

    private String enrollmentDate;

    public Enrollment() {}

    public Enrollment(Student student, Course course, String enrollmentDate) {
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
    }

    public Enrollment(ObjectId studentId, ObjectId courseId, String enrollmentDate) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
    }

    // Getters, setters, toString()

    @Override
    public String toString() {
        if (student != null && course != null) {
            return "Embedded Enrollment{" + student + ", " + course + ", date=" + enrollmentDate + "}";
        } else {
            return "Referenced Enrollment{studentId=" + studentId + ", courseId=" + courseId + ", date=" + enrollmentDate + "}";
        }
    }
}
