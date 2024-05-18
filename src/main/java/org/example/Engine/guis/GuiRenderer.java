package org.example.Engine.guis;

import org.example.Engine.Loader;
import org.example.Engine.models.RawModel;
import org.example.Engine.toolbox.Maths;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class GuiRenderer {

    private final RawModel quad;
    private GuiShader shader;

    /**
     * Constructs a GuiRenderer to render GUI textures.
     *
     * @param loader The loader used to load the GUI quad model.
     */
    public GuiRenderer(Loader loader) {
        float[] positions = { -1, 1, -1, -1, 1, 1, 1, -1 };
        quad = loader.loadToVAO(positions, 2);
        shader = new GuiShader();
    }

    /**
     * Renders the list of GUI textures.
     *
     * @param guis The list of GUI textures to render.
     */
    public void render(List<GuiTexture> guis) {
        shader.start();
        GL30.glBindVertexArray(quad.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        for (GuiTexture gui : guis) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, gui.getTexture());
            Matrix4f matrix = Maths.createTransformationMatrix(gui.getPosition(), gui.getScale());
            shader.loadTransformation(matrix);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    /**
     * Cleans up the resources used by the GUI renderer.
     */
    public void cleanUp() {
        shader.cleanUp();
    }
}
