package org.example.Engine;

import org.example.Engine.models.RawModel;
import org.example.Engine.terrains.Terrain;
import org.example.Engine.terrains.TerrainShader;
import org.example.Engine.textures.TerrainTexturePack;
import org.example.Engine.toolbox.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class TerrainRenderer {

    private TerrainShader shader;
    private float shadingLevels = 20.0f;

    /**
     * Constructs a TerrainRenderer with the specified shader and projection matrix.
     *
     * @param shader The shader program to use for terrain rendering.
     * @param projectionMatrix The projection matrix to be loaded into the shader.
     */
    public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.connectTextureUnits();
        shader.stop();
    }

    /**
     * Renders a list of terrains.
     *
     * @param terrains The list of terrains to render.
     */
    public void render(List<Terrain> terrains) {
        shader.loadShadingLevels(shadingLevels);

        for (Terrain terrain : terrains) {
            prepareTerrain(terrain);
            loadModelMatrix(terrain);
            int vertexCount = terrain.getModel().getVertexCount();
            GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
            unbindTerrain();
        }
    }

    /**
     * Prepares a terrain for rendering by binding the VAO and enabling vertex attribute arrays.
     *
     * @param terrain The terrain to prepare.
     */
    public void prepareTerrain(Terrain terrain) {
        RawModel rawModel = terrain.getModel();
        GL30.glBindVertexArray(rawModel.getVaoID());
        GL20.glEnableVertexAttribArray(0); // position
        GL20.glEnableVertexAttribArray(1); // textureCoordinates
        GL20.glEnableVertexAttribArray(2); // normal
        bindTextures(terrain);
        shader.loadShineVariables(1, 0);
    }

    /**
     * Binds the textures associated with the terrain.
     *
     * @param terrain The terrain whose textures are to be bound.
     */
    private void bindTextures(Terrain terrain) {
        TerrainTexturePack texturePack = terrain.getTexturePack();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getBackgroundTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getrTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getgTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getbTexture().getTextureID());
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getBlendMap().getTextureID());
    }

    /**
     * Unbinds the terrain by disabling vertex attribute arrays and unbinding the VAO.
     */
    public void unbindTerrain() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    /**
     * Loads the model matrix of the terrain into the shader.
     *
     * @param terrain The terrain whose model matrix is to be loaded.
     */
    private void loadModelMatrix(Terrain terrain) {
        Vector3f translation = terrain.getPosition();
        float rx = 0; // rotation around the x-axis
        float ry = 0; // rotation around the y-axis
        float rz = 0; // rotation around the z-axis
        float scale = 1; // scale factor

        Matrix4f transformationMatrix = Maths.createTransformationMatrix(
                translation, rx, ry, rz, scale);
        shader.loadTransformationMatrix(transformationMatrix);
    }
}
