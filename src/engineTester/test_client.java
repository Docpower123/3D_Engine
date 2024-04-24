package com.example.engineTester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

public class test_client {

    private static CountDownLatch latch;
    private static volatile String[] world_packet;
    private static Socket client; // Client socket

    static class Networking implements Runnable {
        private OutputStream outputStream; // Output stream for sending data

        public void send(String data) throws IOException {
            outputStream.write(data.getBytes());
            outputStream.flush();
        }

        @Override
        public void run() {
            String ip = "localhost";
            int port = 12345;
            try {
                client = new Socket(ip, port);
                outputStream = client.getOutputStream(); // Initialize the output stream
                boolean is_first = false;
                String line;
                InputStream data = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(data));
                while (true) {
                    if (!is_first && (line = reader.readLine()) != null) {
                        world_packet = line.substring(1, line.length() - 1).split(", ");
                        is_first = true;
                        latch.countDown(); // Signal that the packet is received
                    } else {
                        if ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        } else {
                            // Handle the case where the server closes the connection
                            System.out.println("Server closed the connection.");
                            break;
                        }
                    }
                }
            } catch (SocketException e) {
                // Handle socket exceptions (connection reset)
                System.out.println("Connection reset by server.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    client.close(); // Close the client socket
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void stop(){
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        latch = new CountDownLatch(1); // Initialize latch with count 1
        Networking networking = new Networking();
        Thread thread = new Thread(networking);
        thread.start();
        latch.await();
        System.out.println("Received packet: " + String.join(", ", world_packet));
        // Send data to the server using the same client socket
        if (networking != null) {
            networking.send("Hello, server!");
        }
        networking.stop();
    }
}
