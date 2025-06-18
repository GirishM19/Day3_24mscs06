package org.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.util.MongoUtil;

import java.util.Date;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        MongoDatabase db = MongoUtil.getDatabase();
        MongoCollection<Document> studentsCol = db.getCollection("students");
        MongoCollection<Document> coursesCol = db.getCollection("courses");
        MongoCollection<Document> enrollmentsCol = db.getCollection("enrollments");

        // Index for efficient search by student name
        studentsCol.createIndex(Indexes.ascending("name"));

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n🔷 MENU");
            System.out.println("1. Add student, course, and enrollment");
            System.out.println("2. Update student name");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addEnrollmentsByType(scanner, studentsCol, coursesCol, enrollmentsCol);
                    break;
                case "2":
                    updateStudentName(scanner, studentsCol, coursesCol, enrollmentsCol);
                    break;
                case "0":
                    running = false;
                    System.out.println("👋 Exiting...");
                    break;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        }

        scanner.close();
    }

    private static void addEnrollmentsByType(Scanner scanner,
                                             MongoCollection<Document> studentsCol,
                                             MongoCollection<Document> coursesCol,
                                             MongoCollection<Document> enrollmentsCol) {
        System.out.println("\n👤 Enter Student Details:");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        Document studentDoc = studentsCol.find(Filters.eq("email", email)).first();

        if (studentDoc == null) {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            Document newStudent = new Document("name", name).append("email", email);
            studentsCol.insertOne(newStudent);
            System.out.println("✅ Student added.");

            // Re-fetch to get the generated _id
            studentDoc = studentsCol.find(Filters.eq("email", email)).first();
        } else {
            System.out.println("ℹ️ Student already exists. Using existing student.");
        }

        System.out.println("\n📘 Enter Course Details:");
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Code: ");
        String code = scanner.nextLine().trim();

        Document newCourse = new Document("title", title).append("code", code);
        coursesCol.insertOne(newCourse);
        System.out.println("✅ Course added.");

        // Re-fetch course to get the generated _id
        Document courseDoc = coursesCol.find(Filters.and(Filters.eq("title", title), Filters.eq("code", code))).first();

        ObjectId studentId = studentDoc.getObjectId("_id");
        ObjectId courseId = courseDoc.getObjectId("_id");

        // Ask user for enrollment type
        System.out.println("\nChoose enrollment type:");
        System.out.println("1. Embedded (student and course data embedded)");
        System.out.println("2. Referenced (studentId and courseId only)");
        System.out.print("Enter choice (1 or 2): ");
        String typeChoice = scanner.nextLine().trim();

        if ("1".equals(typeChoice)) {
            Document embeddedEnrollment = new Document("type", "embedded")
                    .append("student", studentDoc)
                    .append("course", courseDoc)
                    .append("enrollmentDate", new Date());

            enrollmentsCol.insertOne(embeddedEnrollment);
            System.out.println("✅ Embedded enrollment added.");
        } else if ("2".equals(typeChoice)) {
            Document referencedEnrollment = new Document("type", "referenced")
                    .append("studentId", studentId)
                    .append("courseId", courseId)
                    .append("enrollmentDate", new Date());

            enrollmentsCol.insertOne(referencedEnrollment);
            System.out.println("✅ Referenced enrollment added.");
        } else {
            System.out.println("❌ Invalid enrollment type choice. No enrollment added.");
        }

        System.out.println("\n📦 All Enrollments:");
        for (Document doc : enrollmentsCol.find()) {
            System.out.println(doc.toJson());
        }
    }

    private static void updateStudentName(Scanner scanner,
                                          MongoCollection<Document> studentsCol,
                                          MongoCollection<Document> coursesCol,
                                          MongoCollection<Document> enrollmentsCol) {
        System.out.print("\n🔧 Enter student email to update: ");
        String email = scanner.nextLine().trim();

        Document studentDoc = studentsCol.find(Filters.eq("email", email)).first();
        if (studentDoc == null) {
            System.out.println("❌ Student not found.");
            return;
        }

        ObjectId studentId = studentDoc.getObjectId("_id");

        System.out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();
        studentsCol.updateOne(Filters.eq("_id", studentId), Updates.set("name", newName));
        System.out.println("✅ Student name updated.");

        // Show referenced enrollment - it reflects updated student info automatically
        Document refEnroll = enrollmentsCol.find(Filters.eq("studentId", studentId)).first();
        if (refEnroll != null) {
            Document updatedStudent = studentsCol.find(Filters.eq("_id", studentId)).first();
            Document courseDoc = coursesCol.find(Filters.eq("_id", refEnroll.getObjectId("courseId"))).first();

            System.out.println("\n🔗 Referenced Enrollment (Name is updated automatically):");
            System.out.println("Student: " + updatedStudent.toJson());
            System.out.println("Course: " + courseDoc.toJson());
        }

        // Show embedded enrollment - it still contains the old student data (stale)
        Document embedded = enrollmentsCol.find(Filters.eq("type", "embedded")).first();
        if (embedded != null) {
            System.out.println("\n📂 Embedded Enrollment (Still contains old student info):");
            System.out.println(embedded.toJson());
        }

        System.out.println("\n🧠 Difference:");
        System.out.println("🔹 Referenced enrollments reflect changes in original documents automatically.");
        System.out.println("🔸 Embedded enrollments are static snapshots and do not update automatically.");
    }
}
