package org.example;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        GameClient client = new GameClient("localhost", 12345);
        client.setPositionUpdateConsumer(update -> {
            System.out.println("Received position update: " + update);
        });

        new Thread(client).start();
        Thread.sleep(1000);
        // Example of sending data to the server
        client.sendToServer("Hello Server!");

        // Get initial world data
        String worldData = client.getWorldData();
        System.out.println("World Data: " + worldData);
    }
}
