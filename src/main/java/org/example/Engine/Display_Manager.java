package org.example.Engine;

import org.example.Engine.input.Keyboard;
import org.example.Engine.input.Mouse;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
import static java.lang.System.*;

public class Display_Manager {

    private static String title;
    private static int width = 800;
    private static int height = 600;
    private static long window;

    private static long lastFrameTime = getCurrentTime();
    private static float delta = 1.0f / 60f;

    private static long oldNanoTime = 0;
    private static int frames = 0;

    //Creates the display window with the specified title
    public static void createDisplay(String Title) {
        title = Title;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        // configure the version
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        // Enable VSync
        glfwSwapInterval(1);
        glfwMakeContextCurrent(window);
        createCapabilities();

        // Set framebuffer size callback
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            width = w;
            height = h;
            glViewport(0, 0, width, height);
        });
        Keyboard.setWindow(window);
        Mouse.setWindow(window);
    }

    /**
     * Updates the display by polling events and swapping buffers.
     * Also calculates the frame time and updates the window title with FPS.
     */
    public static void updateDisplay() {
        glfwPollEvents();
        glfwSwapBuffers(window);
        long currentFrameTime = getCurrentTime();
        delta = (currentFrameTime - lastFrameTime) / 1000f;
        lastFrameTime = currentFrameTime;

        // fps calculation
        frames += 1;
        long nanoTime = System.nanoTime();
        long deltaTime = nanoTime - oldNanoTime;
        if (deltaTime > 1000000000) {
            if (oldNanoTime > 0) {
                double seconds = deltaTime * 1e-9;
                double fps = frames / seconds;
                glfwSetWindowTitle(window, title + " - FPS: " + (int) fps);
                frames = 0;
            }
            oldNanoTime = nanoTime;
        }
    }

    /**
     * Closes the display by freeing callbacks and destroying the window.
     */
    public static void closeDisplay() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    /**
     * Gets the time in seconds for the last frame.
     *
     * @return The time in seconds for the last frame.
     */
    public static float getFrameTimeSeconds() {
        return delta;
    }

    /**
     * Checks if the window close request has been made.
     *
     * @return true if the window close request has been made, false otherwise.
     */
    public static boolean isCloseRequested() {
        return glfwWindowShouldClose(window);
    }

    /**
     * Returns the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    private static long getCurrentTime() {
        return currentTimeMillis();
    }

    /**
     * Gets the width of the display window.
     *
     * @return The width of the display window.
     */
    public static int getWidth() {
        return width;
    }

    /**
     * Gets the height of the display window.
     *
     * @return The height of the display window.
     */
    public static int getHeight() {
        return height;
    }
}
