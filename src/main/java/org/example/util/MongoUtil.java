package org.example.util;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoUtil {
    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "school";

    private static MongoClient mongoClient = MongoClients.create(URI);

    public static MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DB_NAME);
    }
}
