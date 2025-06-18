package org.example.model;



import org.bson.types.ObjectId;

public class Course {
    private ObjectId id;
    private String title;
    private String code;

    public Course() {}

    public Course(String title, String code) {
        this.title = title;
        this.code = code;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @Override
    public String toString() {
        return "Course{title='" + title + "', code='" + code + "'}";
    }
}
