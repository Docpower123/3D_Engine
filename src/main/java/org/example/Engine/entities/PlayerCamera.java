package org.example.Engine.entities;

import org.example.Engine.input.Keyboard;
import org.joml.Vector3f;

public class PlayerCamera implements Camera {

    private Vector3f position = new Vector3f(0, 0, 0);
    // high or low
    private float pitch = 15;
    // left or right
    private float yaw;
    private float roll;

    public PlayerCamera() {

    }

    public void move() {
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            position.z -= 1f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            position.z += 1f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            position.x += 1f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            position.x -= 1f;
        }
    }
    
    public void invertPitch() {
        this.pitch = -pitch;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void printPosition() {
        System.out.println("Camera Pos: (" + position.get(0) + ", " + position.get(1) + ", " + position.get(2) + ")");
    }
}
