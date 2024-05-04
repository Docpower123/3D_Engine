package org.example.Engine.entities;

import org.example.Engine.models.TexturedModel;
import org.joml.Vector3f;

public class Enemy extends Entity {

    private int health = 100;
    private static final float ATTACK_DISTANCE = 5.0f; // Distance at which enemy starts attacking
    private static final int DAMAGE_COOLDOWN = 300; // Cooldown duration in milliseconds
    private long lastDamageTime = 0;

    public Enemy(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(model, position, rotX, rotY, rotZ, scale);
    }

    public void moving(Vector3f playerPos, Player player) {
        // Calculate direction vector from enemy to player
        Vector3f direction = new Vector3f(playerPos);
        direction.sub(super.getPosition()); // Subtract enemy position from player position

        // Calculate the distance between the enemy and the player
        float distance = direction.length();

        // Normalize direction vector
        direction.normalize();

        // Define the stopping distance from the player
        float stoppingDistance = ATTACK_DISTANCE;

        // If the distance is greater than the stopping distance, move the enemy towards the player
        if (distance > stoppingDistance) {
            // Move the enemy towards the player
            float speed = 0.5f;
            super.increasePosition(direction.x * speed, 0, direction.z * speed);
        } else {
            // Enemy is near the player, deal damage
            player.takeDamage(10); // Assuming 10 damage per attack, you can adjust as needed
        }
    }

    public void takeDamage(int amount) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime > DAMAGE_COOLDOWN) {
            health -= amount;
            if (health <= 0) {
                die();
            }
            lastDamageTime = currentTime;
        }
    }


    public int getHealth() {
        return health;
    }

    private void die() {
        System.out.println("Enemy died!");
    }
}

