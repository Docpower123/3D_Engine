package com.example.Engine.renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.example.Engine.entities.Camera;
import com.example.Engine.entities.Entity;
import com.example.Engine.entities.Light;
import com.example.Engine.models.TexturedModel;
import com.example.Engine.shaders.StaticShader30;
import com.example.Engine.skybox.Sky;
import com.example.Engine.skybox.SkyboxRenderer30;
import com.example.Engine.terrains.Terrain;
import com.example.Engine.terrains.TerrainShader30;
import com.example.Engine.toolbox.Maths;

/**
 * The MasterRenderer class is responsible for rendering entities, terrains, and the skybox in the scene.
 */
public class MasterRenderer {

    private static final float FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 100000;

    private Matrix4f projectionMatrix;

    private StaticShader30 shader = new StaticShader30();
    private EntityRenderer30 renderer;

    private TerrainRenderer30 terrainRenderer;
    private TerrainShader30 terrainShader = new TerrainShader30();

    private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
    private List<Terrain> terrains = new ArrayList<>();

    private SkyboxRenderer30 skyboxRenderer;

    /**
     * Constructor for MasterRenderer.
     * @param loader The loader object to load resources.
     */
    public MasterRenderer(Loader loader) {
        enableCulling();
        projectionMatrix = Maths.createProjectionMatrix(FOV, NEAR_PLANE, FAR_PLANE);
        renderer = new EntityRenderer30(shader, projectionMatrix);
        terrainRenderer = new TerrainRenderer30(terrainShader, projectionMatrix);
        skyboxRenderer = new SkyboxRenderer30(loader, projectionMatrix);
    }

    /**
     * Enables back-face culling.
     */
    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }

    /**
     * Disables back-face culling.
     */
    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    /**
     * Renders the entire scene.
     * @param entities The list of entities to render.
     * @param terrains The list of terrains to render.
     * @param lights The list of lights in the scene.
     * @param sky The sky object representing the sky in the scene.
     * @param camera The camera object representing the viewpoint.
     * @param clipPlane The clipping plane equation.
     */
    public void renderScene(List<Entity> entities, List<Terrain> terrains,
                            List<Light> lights, Sky sky, Camera camera, Vector4f clipPlane) {
        for (Entity entity : entities) {
            processEntity(entity);
        }
        for (Terrain terrain : terrains) {
            processTerrain(terrain);
        }
        render(lights, sky, camera, clipPlane);
    }

    /**
     * Renders the scene.
     * @param lights The list of lights in the scene.
     * @param sky The sky object representing the sky in the scene.
     * @param camera The camera object representing the viewpoint.
     * @param clipPlane The clipping plane equation.
     */
    public void render(List<Light> lights, Sky sky, Camera camera, Vector4f clipPlane) {
        prepare(sky);

        shader.start();
        shader.loadClipPlane(clipPlane);
        shader.loadSkyColor(sky.getColor());
        shader.loadSkyVariables(sky.getDensity(), sky.getGradient());
        shader.loadLights(lights);
        shader.loadViewMatrix(camera);
        renderer.render(entities);
        shader.stop();

        terrainShader.start();
        terrainShader.loadClipPlane(clipPlane);
        terrainShader.loadSkyColor(sky.getColor());
        terrainShader.loadSkyVariables(sky.getDensity(), sky.getGradient());
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains);
        terrainShader.stop();

        skyboxRenderer.render(camera, sky.getColor());

        entities.clear();
        terrains.clear();
    }

    /**
     * Processes a terrain to be rendered.
     * @param terrain The terrain object to process.
     */
    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    /**
     * Processes an entity to be rendered.
     * @param entity The entity object to process.
     */
    public void processEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);
        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }
    }

    /**
     * Cleans up resources used by the renderer.
     */
    public void cleanUp() {
        shader.cleanUp();
        terrainShader.cleanUp();
    }

    /**
     * Prepares the rendering process.
     * @param sky The sky object representing the sky in the scene.
     */
    public void prepare(Sky sky) {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        Vector3f skyColor = sky.getColor();
        GL11.glClearColor(skyColor.x, skyColor.y, skyColor.z, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Retrieves the projection matrix.
     * @return The projection matrix.
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * Retrieves the near plane distance.
     * @return The near plane distance.
     */
    public float getNearPlane() {
        return NEAR_PLANE;
    }

    /**
     * Retrieves the far plane distance.
     * @return The far plane distance.
     */
    public float getFarPlane() {
        return FAR_PLANE;
    }
}
