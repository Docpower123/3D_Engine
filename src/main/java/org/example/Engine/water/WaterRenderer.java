package org.example.Engine.water;


import org.example.Engine.Display_Manager;
import org.example.Engine.Loader;
import org.example.Engine.entities.Camera;
import org.example.Engine.entities.Light;
import org.example.Engine.models.RawModel;
import org.example.Engine.skybox.Sky;
import org.example.Engine.toolbox.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class WaterRenderer {
    
    private static final String DUDV_MAP = "waterDUDV";
    private static final String NORMAL_MAP = "normalMap";
    
    private static final float WAVE_SPEED = 0.03f;

    private RawModel quad;
    private WaterShader shader;
    private WaterFrameBuffers fbos;
    private float tiling = 100f;
    
    private float moveFactor = 0f;
    private float waveStrength = 0.04f;
    
    private int dudvTexture;
    private int normalMap;
    
    private float shadingLevels = 10.0f;
    
    public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix,
                         WaterFrameBuffers fbos) {
        this.shader = shader;
        this.fbos = fbos;
        dudvTexture = loader.loadTexture(DUDV_MAP);
        normalMap = loader.loadTexture(NORMAL_MAP);
        shader.start();
        shader.connectTextureUnits();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
        setUpVAO(loader);
    }

    public void render(List<WaterTile> water, Sky sky, Camera camera, List<Light> lights) {
        prepareRender(sky, camera, lights);
        for (WaterTile tile : water) {
            Matrix4f modelMatrix = Maths.createTransformationMatrix(
                    new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()),
                    0, 0, 0, tile.getSize());
            shader.loadModelMatrix(modelMatrix);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
        }
        unbind();
    }
    
    private void prepareRender(Sky sky, Camera camera, List<Light> lights) {
        shader.start();
        shader.loadViewMatrix(camera);
        shader.loadTiling(tiling);
        moveFactor += WAVE_SPEED * Display_Manager.getFrameTimeSeconds();
        moveFactor %= 1;
        shader.loadMoveFactor(moveFactor);
        shader.loadWaveStrength(waveStrength); // set waveStrength to 0 to remove the dudvMap distortion
        shader.loadLights(lights);

        shader.loadSkyColor(sky.getColor());
        shader.loadSkyVariables(sky.getDensity(), sky.getGradient());
        shader.loadShadingLevels(shadingLevels);
       
        GL30.glBindVertexArray(quad.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getReflectionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMap);
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    private void unbind() {
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    private void setUpVAO(Loader loader) {
        float[] vertices = {
                 0, 0,
                 0, 1,
                 1, 0,
                 1, 0,
                 0, 1,
                 1, 1 };
        quad = loader.loadToVAO(vertices, 2);
    }

}
