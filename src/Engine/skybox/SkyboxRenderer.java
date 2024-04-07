package com.example.Engine.skybox;

import com.example.Engine.entities.Camera;
import com.example.Engine.models.RawModel;
import com.example.Engine.renderEngine.DisplayManager;
import com.example.Engine.renderEngine.Loader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SkyboxRenderer {

    private static String[] TEXTURE_FILES = {
        "skyRight", "skyLeft", "skyTop", "skyBottom", "skyBack", "skyFront"
    };

    private static String[] NIGHT_TEXTURE_FILES = {
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

    public void render(Camera camera, Vector3f skyColor) {
        render(camera, skyColor.x, skyColor.y, skyColor.z);
    }
    
    public void render(Camera camera, float r, float g, float b) {
        shader.start();
        shader.loadViewMatrix(camera);
        shader.loadSkyColor(r, g, b);
        shader.loadLimits(SIZE/50, SIZE/3);
        shader.loadShadingLevels(shadingLevels);
        GL30.glBindVertexArray(cube.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        bindTextures();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    private void bindTextures() {
        time += DisplayManager.getFrameTimeSeconds() * 100; // 100 instead of 1000 to slow down a bit

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