package org.example;

import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class GameClient implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String serverAddress;
    private final int serverPort;
    private boolean running = true;
    private String worldData;
    private Consumer<String> onPositionUpdateConsumer;
    private Map<String, Vector3f> playerPositions = new ConcurrentHashMap<>();
    private boolean firstLine = false;

    public GameClient(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    public void setPositionUpdateConsumer(Consumer<String> consumer) {
        this.onPositionUpdateConsumer = consumer;
    }

    public String getWorldData() {
        firstLine = true;
        return worldData;
    }

    public Map<String, Vector3f> getPlayerPositions() {
        return new ConcurrentHashMap<>(playerPositions);  // Return a copy to avoid modification outside
    }

    @Override
    public void run() {
        try {
            connect();
            while (running) {
                Thread.sleep(2000);  // Throttle the updates
            }
        } catch (IOException e) {
            System.out.println("Could not connect to the server: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Client was interrupted: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void connect() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Initial data fetch (displayed locally)
        worldData = in.readLine();
        Thread listenerThread = new Thread(this::listen);
        listenerThread.start();
    }

    private void listen() {
        try {
            String inputLine;
            // Assume the first line is already handled as world data
            while ((inputLine = in.readLine()) != null && running && firstLine) {
                // Split multiple updates contained in one line
                String[] updates = inputLine.split(";");
                for (String update : updates) {
                    if (update.isEmpty()) continue; // Skip empty updates

                    // Extract client info and coordinates
                    // Expected format: ('IP', PORT); X,Y,Z
                    int index = update.indexOf("); ");
                    if (index == -1) continue; // Malformed input, no closing parenthesis followed by semicolon

                    String clientInfo = update.substring(0, index + 1); // Includes ('IP', PORT)
                    String coords = update.substring(index + 3).trim(); // X,Y,Z part

                    if (!coords.isEmpty()) {
                        String[] locSplit = coords.split(",");
                        Vector3f loc = new Vector3f(Float.parseFloat(locSplit[0]),
                                Float.parseFloat(locSplit[1]),
                                Float.parseFloat(locSplit[2]));

                        // Use the client info as the key, or process it further to extract just the IP or another unique identifier
                        playerPositions.put(clientInfo, loc);

                        // Optionally, notify about the position update
                        if (onPositionUpdateConsumer != null) {
                            onPositionUpdateConsumer.accept(clientInfo + " updated to " + loc.toString());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from server: " + e.getMessage());
            close();  // Close connection on error
        }
    }


    public void sendPlayerPosition(Vector3f position) {
        // Format the position into a string with labels for x, y, and z coordinates
        String positionUpdate = String.format("%.2f,%.2f,%.2f", position.x, position.y, position.z);
        sendToServer(positionUpdate);
    }

    public void sendToServer(String data) {
        if (out != null) {
            out.println(data);
        }
    }

    private void close() {
        running = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }

    public void stopClient() {
        running = false;
        close();
    }
}
