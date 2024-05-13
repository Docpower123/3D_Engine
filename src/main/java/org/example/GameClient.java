package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
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

    public GameClient(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    public void setPositionUpdateConsumer(Consumer<String> consumer) {
        this.onPositionUpdateConsumer = consumer;
    }

    public String getWorldData() {
        return worldData;
    }

    @Override
    public void run() {
        try {
            connect();
            while (running) {
                sendPositionUpdates();
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
        if (worldData != null) {
        }

        Thread listenerThread = new Thread(this::listen);
        listenerThread.start();
    }

    private void listen() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null && running) {
                if (onPositionUpdateConsumer != null) {
                    onPositionUpdateConsumer.accept(inputLine);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from server: " + e.getMessage());
        }
    }

    public void sendToServer(String data) {
        if (out != null) {
            out.println(data);
        }
    }

    private void sendPositionUpdates() {
        Random random = new Random();
        int x = random.nextInt(100);
        int y = random.nextInt(100);
        String positionUpdate = "Position x:" + x + " y:" + y;
        sendToServer(positionUpdate);
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
