package org.example.Engine.input;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

    private static HashMap<Integer, String> keysDown = new HashMap<Integer, String>();

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            keysDown.put(key, "down");
        }
        else if (action == GLFW_RELEASE) {
            keysDown.remove(key);
        }
    }

    public static void setWindow(long window) {
        glfwSetKeyCallback(window, Keyboard::keyCallback);        
    }

    // The keys we are using.
    public static int KEY_W     = GLFW_KEY_W;
    public static int KEY_S     = GLFW_KEY_S;
    public static int KEY_D     = GLFW_KEY_D;
    public static int KEY_A     = GLFW_KEY_A;
    public static int KEY_SPACE = GLFW_KEY_SPACE;
    public static int KEY_UP    = GLFW_KEY_UP;
    public static int KEY_DOWN  = GLFW_KEY_DOWN;
    public static int KEY_RIGHT = GLFW_KEY_RIGHT;
    public static int KEY_LEFT  = GLFW_KEY_LEFT;

    public static int KEY_T  = GLFW_KEY_T;

    public static boolean isKeyDown(int key) {
        return keysDown.containsKey(key);
    }
}
