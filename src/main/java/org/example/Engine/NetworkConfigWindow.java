package org.example.Engine;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Scanner;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class NetworkConfigWindow {
    private long window;
    private int width = 600;
    private int height = 400;
    private String ipAddress = "127.0.0.1"; // Default IP
    private int port = 12345; // Default port

    public NetworkConfigWindow() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter IP Address (default 127.0.0.1): ");
        ipAddress = scanner.nextLine();
        if (ipAddress.isEmpty()) {
            ipAddress = "127.0.0.1";
        }

        System.out.print("Enter Port (default 12345): ");
        try {
            port = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            port = 12345; // Default if invalid input
        }
    }

    public void run() {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(width, height, "Network Configuration", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                System.out.println("IP: " + ipAddress + ", Port: " + port);
                glfwSetWindowShouldClose(window, true); // Close the window on ENTER key press
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.6f, 0.7f, 0.8f, 0.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new NetworkConfigWindow().run();
    }
}
