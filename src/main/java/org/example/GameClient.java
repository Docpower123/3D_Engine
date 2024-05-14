package org.example;

import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
    private Map<String, Integer> playerHealth = new ConcurrentHashMap<>();
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

    public Map<String, Integer> getPlayerhealth() {
        return new ConcurrentHashMap<>(playerHealth);  // Return a copy to avoid modification outside
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
                System.out.println(inputLine);
                String[] players = inputLine.split("/p");
                for (String player : players) {
                    String[] entryComponents = player.split(";");
                    if (entryComponents.length > 1) {
                        String playerInfo = entryComponents[0];
                        String[] positionAndHealth = entryComponents[1].split("\\*");
                        if (positionAndHealth.length == 2) {
                            String fixed_pos = positionAndHealth[0].substring(3,positionAndHealth[0].length()-2);
                            String[] loc_split = fixed_pos.split(",");
                            Vector3f loc = new Vector3f(
                                    Float.parseFloat(loc_split[0]),
                                    Float.parseFloat(loc_split[1]),
                                    Float.parseFloat(loc_split[2])
                            );
                            int health = Integer.parseInt(positionAndHealth[1]);
                            if (onPositionUpdateConsumer != null) {
                                onPositionUpdateConsumer.accept(player);
                            } else {
                                playerPositions.put(playerInfo, loc);
                                playerHealth.put(playerInfo, health);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from server: " + e.getMessage());
            close();  // Close connection on error
        }
    }


    public void sendPlayerPosition(Vector3f position, int health) {
        // Format the position into a string with labels for x, y, and z coordinates
        String positionUpdate = String.format("%.2f,%.2f,%.2f*%d", position.x, position.y, position.z, health);
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
