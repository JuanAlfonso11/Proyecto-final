package org.example.utils;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb+srv://" + EnvServices.getInstance().result("USER") + ":" + EnvServices.getInstance().result("PASS") + "@examnen.ubl4qnk.mongodb.net/?retryWrites=true&w=majority";


    public static MongoClient connect() {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        return mongoClient;
    }
}