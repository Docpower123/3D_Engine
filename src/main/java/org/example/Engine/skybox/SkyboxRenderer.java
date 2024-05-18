package org.example.Engine.skybox;

import org.example.Engine.Display_Manager;
import org.example.Engine.Loader;
import org.example.Engine.entities.Camera;
import org.example.Engine.models.RawModel;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class SkyboxRenderer {

    private static final String[] TEXTURE_FILES = {
            "skyRight", "skyLeft", "skyTop", "skyBottom", "skyBack", "skyFront"
    };

    private static final String[] NIGHT_TEXTURE_FILES = {
            "nightRight", "nightLeft", "nightTop", "nightBottom", "nightBack", "nightFront"
    };

    private RawModel cube;
    private int texture;
    private int nightTexture;
    private SkyboxShader shader;
    private float time = 0;
    private float shadingLevels = 10.0f;

    private static final float SIZE = 50000f;

    private static final float[] VERTICES = {
            -SIZE, SIZE, -SIZE,
            -SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, SIZE, -SIZE,
            -SIZE, SIZE, -SIZE,

            -SIZE, -SIZE, SIZE,
            -SIZE, -SIZE, -SIZE,
            -SIZE, SIZE, -SIZE,
            -SIZE, SIZE, -SIZE,
            -SIZE, SIZE, SIZE,
            -SIZE, -SIZE, SIZE,

            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,

            -SIZE, -SIZE, SIZE,
            -SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            SIZE, -SIZE, SIZE,
            -SIZE, -SIZE, SIZE,

            -SIZE, SIZE, -SIZE,
            SIZE, SIZE, -SIZE,
            SIZE, SIZE, SIZE,
            SIZE, SIZE, SIZE,
            -SIZE, SIZE, SIZE,
            -SIZE, SIZE, -SIZE,

            -SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE, SIZE,
            SIZE, -SIZE, -SIZE,
            SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE, SIZE,
            SIZE, -SIZE, SIZE
    };

    /**
     * Constructs a SkyboxRenderer.
     *
     * @param loader The loader used to load the skybox textures.
     * @param projectionMatrix The projection matrix for rendering the skybox.
     */
    public SkyboxRenderer(Loader loader, Matrix4f projectionMatrix) {
        cube = loader.loadToVAO(VERTICES, 3);
        texture = loader.loadCubeMap(TEXTURE_FILES);
        nightTexture = loader.loadCubeMap(NIGHT_TEXTURE_FILES);
        shader = new SkyboxShader();
        shader.start();
        shader.connectTextureUnits();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    /**
     * Renders the skybox.
     *
     * @param camera The camera used to view the skybox.
     * @param skyColor The color of the sky.
     */
    public void render(Camera camera, Vector3f skyColor) {
        render(camera, skyColor.x, skyColor.y, skyColor.z);
    }

    /**
     * Renders the skybox.
     *
     * @param camera The camera used to view the skybox.
     * @param r The red component of the sky color.
     * @param g The green component of the sky color.
     * @param b The blue component of the sky color.
     */
    public void render(Camera camera, float r, float g, float b) {
        shader.start();
        shader.loadViewMatrix(camera);
        shader.loadSkyColor(r, g, b);
        shader.loadLimits(SIZE / 50, SIZE / 3);
        shader.loadShadingLevels(shadingLevels);
        GL30.glBindVertexArray(cube.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        bindTextures();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    /**
     * Binds the day and night textures to the shader.
     */
    private void bindTextures() {
        time += Display_Manager.getFrameTimeSeconds() * 100;
        time %= 24000;
        int texture1;
        int texture2;
        float blendFactor;
        if (time >= 0 && time < 12000) {
            texture1 = nightTexture;
            texture2 = texture;
            blendFactor = (time - 0) / (12000 - 0);
        } else {
            texture1 = texture;
            texture2 = nightTexture;
            blendFactor = (time - 12000) / (24000 - 12000);
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture1);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture2);
        shader.loadBlendFactor(blendFactor);
    }
}
