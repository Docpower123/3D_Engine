package org.example.Engine;

import org.example.Engine.entities.Entity;
import org.example.Engine.models.RawModel;
import org.example.Engine.models.TexturedModel;
import org.example.Engine.shaders.StaticShader;
import org.example.Engine.textures.ModelTexture;
import org.example.Engine.toolbox.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.Map;

public class EntityRenderer {

    private StaticShader shader;
    private float shadingLevels = 10.0f;

    /**
     * Constructs an EntityRenderer with the specified shader and projection matrix.
     *
     * @param shader The shader program to use for rendering entities.
     * @param projectionMatrix The projection matrix to be loaded into the shader.
     */
    public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    /**
     * Renders the entities grouped by their TexturedModel.
     *
     * @param entities A map of TexturedModels to lists of entities to render.
     */
    public void render(Map<TexturedModel, List<Entity>> entities) {
        shader.loadShadingLevels(shadingLevels);

        for (TexturedModel model : entities.keySet()) {
            prepareTexturedModel(model);
            List<Entity> batch = entities.get(model);
            int vertexCount = model.getRawModel().getVertexCount();
            for (Entity entity : batch) {
                prepareInstance(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
            }
            unbindTexturedModel();
        }
    }

    /**
     * Prepares a TexturedModel for rendering by binding its VAO and enabling vertex attribute arrays.
     *
     * @param model The TexturedModel to prepare.
     */
    public void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        GL30.glBindVertexArray(rawModel.getVaoID());
        GL20.glEnableVertexAttribArray(0); // position
        GL20.glEnableVertexAttribArray(1); // textureCoordinates
        GL20.glEnableVertexAttribArray(2); // normal
        ModelTexture texture = model.getTexture();
        shader.loadNumberOfRows(texture.getNumberOfRows());
        if (texture.isHasTransparency()) {
            MasterRenderer.disableCulling();
        }
        shader.loadFakeLightingVariable(texture.isUseFakeLighting());
        shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
    }

    /**
     * Unbinds the currently bound TexturedModel by disabling vertex attribute arrays and unbinding the VAO.
     */
    public void unbindTexturedModel() {
        MasterRenderer.enableCulling();
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    /**
     * Prepares an entity for rendering by loading its transformation matrix and texture offset into the shader.
     *
     * @param entity The entity to prepare.
     */
    private void prepareInstance(Entity entity) {
        Vector3f translation = entity.getPosition();
        float rx = entity.getRotX();
        float ry = entity.getRotY();
        float rz = entity.getRotZ();
        float scale = entity.getScale();

        Matrix4f transformationMatrix = Maths.createTransformationMatrix(
                translation, rx, ry, rz, scale);
        shader.loadTransformationMatrix(transformationMatrix);
        shader.loadTextureOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
    }
}
