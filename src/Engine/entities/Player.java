package com.example.Engine.entities;

import com.example.Engine.terrains.World;
import org.joml.Vector3f;

import com.example.Engine.input.Keyboard;
import com.example.Engine.models.TexturedModel;
import com.example.Engine.renderEngine.DisplayManager;

import java.util.List;

public class Player extends Entity {

    public static final float RUN_SPEED = 40;   // units / second
    public static final float TURN_SPEED = 160; // degrees / second
    public static final float GRAVITY = -50;
    public static final float JUMP_POWER = 30;
    
    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;
    private float upwardsSpeed = 0;

    private boolean isInAir = false;

    private static final int ATTACK_RANGE = 5; // Range of attack in game units
    private static final int DAMAGE_AMOUNT = 10; // Amount of damage dealt per attack
    private static final int ATTACK_COOLDOWN = 1000; // Cooldown duration in milliseconds
    private long lastAttackTime = 0;

    public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(model, position, rotX, rotY, rotZ, scale);
    }
    private int health = 100;

    private static final int DAMAGE_COOLDOWN = 300; // 3000 milliseconds (3 seconds) cooldown after taking damage
    private long lastDamageTime = 0;

    public int getHealth() {
        return health;
    }

    public void move(World world, List<Enemy> enemies) {
        checkInputs();
        if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime > ATTACK_COOLDOWN) {
                lastAttackTime = currentTime;
                attack(enemies);
            }
        }
        
        super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
        float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
        float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
        super.increasePosition(dx, 0, dz);
        
        upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
        super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
        
        float terrainHeight = world.getHeightOfTerrain(getPosition().x, getPosition().z);
        if (super.getPosition().y < terrainHeight) {
            upwardsSpeed = 0;
            isInAir = false;
            super.getPosition().y = terrainHeight;
        }

        // assume all low places are filled with water
        // make player swim so the head stays above surface
        float playerHeight = 5;
        float waterHeight = world.getHeightOfWater(getPosition().x, getPosition().z);
        if (super.getPosition().y < waterHeight - playerHeight) {
            upwardsSpeed = 0;
            isInAir = false;
            super.getPosition().y = waterHeight - playerHeight;
        }
    }



    private void attack(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            Vector3f enemyPos = enemy.getPosition();
            Vector3f playerPos = super.getPosition();
            float distance = playerPos.distance(enemyPos);
            if (distance < ATTACK_RANGE) {
                enemy.takeDamage(DAMAGE_AMOUNT);
            }
        }
    }

    public void takeDamage(int amount) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
            health -= amount;
            lastDamageTime = currentTime;
            System.out.println("Player took " + amount + " damage. Remaining health: " + health);
        } else {
            System.out.println("Player is invincible. Cannot take damage yet.");
        }
    }

    private void jump() {
        if (!isInAir) {
            this.upwardsSpeed = JUMP_POWER;
            isInAir = true;
        }
    }

    private void checkInputs() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            this.currentSpeed = RUN_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            this.currentSpeed = -RUN_SPEED;
        } else {
            this.currentSpeed = 0;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            this.currentTurnSpeed = -TURN_SPEED;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            this.currentTurnSpeed = TURN_SPEED;
        } else {
            this.currentTurnSpeed = 0;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            jump();
        }
    }
}
