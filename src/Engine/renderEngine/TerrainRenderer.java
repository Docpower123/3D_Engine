package com.example.Engine.renderEngine;

import java.util.List;

import com.example.Engine.terrains.TerrainShader;
import com.example.Engine.toolbox.Maths;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.example.Engine.models.RawModel;
import com.example.Engine.terrains.Terrain;
import com.example.Engine.textures.TerrainTexturePack;

public class TerrainRenderer {

    private TerrainShader shader;
    private float shadingLevels = 20.0f;

    public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.connectTextureUnits();
        shader.stop();
    }

    public void render(List<Terrain> terrains) {
        
        shader.loadShadingLevels(shadingLevels);
        
        for (Terrain terrain:terrains) {
            prepareTerrain(terrain);
            loadModelMatrix(terrain);
            int vertexCount = terrain.getModel().getVertexCount();
            GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
            unbindTerrain();
        }
    }

    public void prepareTerrain(Terrain terrain) {
        RawModel rawModel = terrain.getModel();
        GL30.glBindVertexArray(rawModel.getVaoID());
        GL20.glEnableVertexAttribArray(0); // position
        GL20.glEnableVertexAttribArray(1); // textureCoordinates
        GL20.glEnableVertexAttribArray(2); // normal
        bindTextures(terrain);
        shader.loadShineVariables(1, 0);
    }
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
    public void unbindTerrain() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }
    
    private void loadModelMatrix(Terrain terrain) {
        Vector3f translation = terrain.getPosition();
        float rx = 0; //terrain.getRotX();
        float ry = 0; //terrain.getRotY();
        float rz = 0; //terrain.getRotZ();
        float scale = 1;
        
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(
                translation, rx, ry, rz, scale);
        shader.loadTransformationMatrix(transformationMatrix);
    }
}
