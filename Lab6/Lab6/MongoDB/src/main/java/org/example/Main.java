package org.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

public class Main {
    public static void main(String[] args) {
        String uri = "mongodb+srv://parvapatel2612:parva123@cluster1.5ogj2.mongodb.net/"; 
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("E-Commerce");
        MongoCollection<Document> collection = database.getCollection("Orders");

        createOrder(collection, "Smartphone", 1, 899.00);
        createOrder(collection, "Laptop", 2, 1500.00);

        System.out.println("\nOrders in Database:");
        readOrders(collection);

        updateOrder(collection, "Smartphone", 2);

        System.out.println("\nOrders after update:");
        readOrders(collection);

        deleteOrder(collection, "Laptop");

        System.out.println("\nOrders after deletion:");
        readOrders(collection);

        mongoClient.close();
    }

    public static void createOrder(MongoCollection<Document> collection, String product, int quantity, double price) {
        Document order = new Document("product", product)
                .append("quantity", quantity)
                .append("price", price);
        collection.insertOne(order);
        System.out.println("Order inserted: " + order.toJson());
    }

    public static void readOrders(MongoCollection<Document> collection) {
        FindIterable<Document> orders = collection.find();
        for (Document order : orders) {
            System.out.println(order.toJson());
        }
    }

    public static void updateOrder(MongoCollection<Document> collection, String product, int newQuantity) {
        Bson filter = Filters.eq("product", product);
        Bson update = Updates.set("quantity", newQuantity);
        collection.updateOne(filter, update);
        System.out.println("Order updated for product: " + product);
    }

    public static void deleteOrder(MongoCollection<Document> collection, String product) {
        Bson filter = Filters.eq("product", product);
        collection.deleteOne(filter);
        System.out.println("Order deleted for product: " + product);
    }
}
