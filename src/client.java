package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    // Define server address and port
    private static final String SERVER_ADDRESS = "127.0.0.1"; // Server's IP address
    private static final int SERVER_PORT = 43434; // Server's port number

    // Flag to check if it's the first message
    private static boolean firstMessage = true;

    public static void main(String[] args) {
        Socket clientSocket = null;
        try {
            // Connect to the server
            clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to the chat room server.");

            // Start receiving and sending messages in separate threads
            Socket finalClientSocket1 = clientSocket;
            Thread receiveThread = new Thread(() -> receiveMessages(finalClientSocket1));
            receiveThread.start();

            Socket finalClientSocket = clientSocket;
            Thread sendThread = new Thread(() -> sendMessages(finalClientSocket));
            sendThread.start();

            // Wait for threads to finish
            receiveThread.join();
            sendThread.join();

        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred: " + e.getMessage());
        } finally {
            try {
                // Close the socket
                if (clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error while closing socket: " + e.getMessage());
            }
        }
    }

    private static void receiveMessages(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String data;
            while ((data = in.readLine()) != null) {
                // Call first() function only for the first message
                if (firstMessage) {
                    firstMessage = false;
                    first(data);
                }
                System.out.println("Received from server: " + data);
            }
        } catch (IOException e) {
            System.err.println("An error occurred while receiving messages: " + e.getMessage());
        }
    }

    private static void sendMessages(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            while (true) {
                // Send message to the server
                System.out.print("Enter message to send to the chat room: ");
                String message = scanner.nextLine();
                out.println(message);
            }
        } catch (IOException e) {
            System.err.println("An error occurred while sending messages: " + e.getMessage());
        }
    }

    private static void first(String data) {
        String[] splitData = data.split(",");
        String terrainSize = splitData[0];
        String terrainMaxHeight = splitData[1];
        String waterSize = terrainSize;
        // Further processing for the first message
    }
}
