package org.example;

import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
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
    private boolean killed = false;  // Flag to indicate if the player was killed
    private String killedPlayerId;  // Variable to store the ID of the killed player
    private boolean win = false;
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

    public String getip(){
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Filter IPv4 addresses only
                    if (addr.getAddress().length == 4) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Map<String, Integer> getPlayerhealth() {
        return playerHealth;
    }

    public boolean isKilled() {
        return killed;
    }

    public String getKilledPlayerId() {
        return killedPlayerId;
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
                // Check if the inputLine indicates the player was killed
                if (inputLine.equals("You have been killed.")) {
                    killed = true;
                    break;  // Stop processing further input
                }
                if(inputLine.equals("win!")){
                    win = true;
                    break;
                }
                if (inputLine.startsWith("PLAYER_KILLED")) {
                    killedPlayerId = inputLine.split(" ")[1];  // Extract the player ID
                    killedPlayerId = killedPlayerId + " "+inputLine.split(" ")[2];
                    continue;  // Skip further processing for this line
                }

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

    public void sendPlayerPosition(Vector3f position, int health, boolean attack, boolean won) {
        String flag = "false";
        String flag1 = "false";
        if(attack){
            flag1 = "True";
        }
        if(won){
            flag1 = "True";
        }
        String positionUpdate = String.format("%.2f,%.2f,%.2f*%d*%s*%s", position.x, position.y, position.z, health, flag, flag1);
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

    public boolean getwin(){
        return win;
    }

    public void stopClient() {
        running = false;
        close();
    }
}
