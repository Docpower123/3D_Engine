package org.example.Engine.toolbox;

import org.example.Engine.Display_Manager;
import org.example.Engine.entities.Camera;
import org.example.Engine.input.Mouse;
import org.example.Engine.terrains.Terrain;
import org.example.Engine.terrains.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MousePicker {

    private static final int RECURSION_COUNT = 2000;
    private static final float RAY_RANGE = 10000;

    private Vector3f currentRay = new Vector3f();

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Camera camera;

    private World world;
    private Vector3f currentTerrainPoint;

    /**
     * Constructs a MousePicker for calculating the 3D ray from the mouse position.
     *
     * @param camera The camera used to calculate the view matrix.
     * @param projectionMatrix The projection matrix of the scene.
     * @param world The world containing the terrains.
     */
    public MousePicker(Camera camera, Matrix4f projectionMatrix, World world) {
        this.camera = camera;
        this.projectionMatrix = projectionMatrix;
        this.viewMatrix = Maths.createViewMatrix(camera);
        this.world = world;
    }

    /**
     * Gets the current point on the terrain where the mouse is pointing.
     *
     * @return The current terrain point.
     */
    public Vector3f getCurrentTerrainPoint() {
        return currentTerrainPoint;
    }

    /**
     * Gets the current 3D ray from the mouse position.
     *
     * @return The current 3D ray.
     */
    public Vector3f getCurrentRay() {
        return currentRay;
    }

    /**
     * Updates the mouse picker by recalculating the current ray and terrain point.
     */
    public void update() {
        viewMatrix = Maths.createViewMatrix(camera);
        currentRay = calculateMouseRay();
        if (intersectionInRange(0, RAY_RANGE, currentRay)) {
            currentTerrainPoint = binarySearch(0, 0, RAY_RANGE, currentRay);
        } else {
            currentTerrainPoint = null;
        }
    }

    /**
     * Calculates the 3D ray from the current mouse position.
     *
     * @return The calculated 3D ray.
     */
    private Vector3f calculateMouseRay() {
        float mouseX = Mouse.getX();
        float mouseY = Mouse.getY();

        // normalized device space
        Vector2f normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY);
        // homogenous clip space
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1f, 1);
        // eye space
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        // world space
        Vector3f worldRay = toWorldCoords(eyeCoords);
        return worldRay;
    }

    /**
     * Converts eye coordinates to world coordinates.
     *
     * @param eyeCoords The eye coordinates.
     * @return The world coordinates.
     */
    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Vector3f mouseRay = new Vector3f();
        mouseRay.normalize();
        return mouseRay;
    }

    /**
     * Converts clip coordinates to eye coordinates.
     *
     * @param clipCoords The clip coordinates.
     * @return The eye coordinates.
     */
    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Vector4f eyeCoords = new Vector4f();
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    /**
     * Gets the normalized device coordinates from the mouse position.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @return The normalized device coordinates.
     */
    private Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY) {
        float x = (2f * mouseX) / (int) Display_Manager.getWidth() - 1;
        float y = (2f * mouseY) / (int) Display_Manager.getHeight() - 1;
        return new Vector2f(x, y);
    }

    /**
     * Calculates the point on the ray at a specific distance from the camera.
     *
     * @param ray The 3D ray.
     * @param distance The distance from the camera.
     * @return The point on the ray.
     */
    private Vector3f getPointOnRay(Vector3f ray, float distance) {
        Vector3f camPos = camera.getPosition();
        Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
        Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
        return start.add(scaledRay);
    }

    /**
     * Performs a binary search to find the exact intersection point on the terrain.
     *
     * @param count The current recursion count.
     * @param start The start distance.
     * @param finish The end distance.
     * @param ray The 3D ray.
     * @return The intersection point on the terrain.
     */
    private Vector3f binarySearch(int count, float start, float finish, Vector3f ray) {
        float half = start + ((finish - start) / 2f);
        if (count >= RECURSION_COUNT) {
            Vector3f endPoint = getPointOnRay(ray, half);
            Terrain terrain = getTerrain(endPoint.get(0), endPoint.get(2));
            if (terrain != null) {
                return endPoint;
            } else {
                return null;
            }
        }
        if (intersectionInRange(start, half, ray)) {
            return binarySearch(count + 1, start, half, ray);
        } else {
            return binarySearch(count + 1, half, finish, ray);
        }
    }

    /**
     * Checks if there is an intersection with the terrain in the given range.
     *
     * @param start The start distance.
     * @param finish The end distance.
     * @param ray The 3D ray.
     * @return True if there is an intersection, false otherwise.
     */
    private boolean intersectionInRange(float start, float finish, Vector3f ray) {
        Vector3f startPoint = getPointOnRay(ray, start);
        Vector3f endPoint = getPointOnRay(ray, finish);
        return !isUnderGround(startPoint) && isUnderGround(endPoint);
    }

    /**
     * Checks if a point is under the ground.
     *
     * @param testPoint The point to check.
     * @return True if the point is under the ground, false otherwise.
     */
    private boolean isUnderGround(Vector3f testPoint) {
        Terrain terrain = getTerrain(testPoint.get(0), testPoint.get(2));
        float height = 0;
        if (terrain != null) {
            height = terrain.getHeightOfTerrain(testPoint.get(0), testPoint.get(2));
        }
        return testPoint.y < height;
    }

    /**
     * Gets the terrain at the given world coordinates.
     *
     * @param worldX The x coordinate.
     * @param worldZ The z coordinate.
     * @return The terrain at the given coordinates.
     */
    private Terrain getTerrain(float worldX, float worldZ) {
        return world.getTerrain(worldX, worldZ);
    }
}
