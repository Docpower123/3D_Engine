package engine.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Texture {
    private int textureID;
    private int width;
    private int height;

    public Texture(int textureID, int width, int height) {
        this.textureID = textureID;
        this.width = width;
        this.height = height;
    }

    public void bind() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
    }

    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTextureID() {
        return textureID;
    }

    public float getImageWidth() {
        return (float) width;
    }

    public float getImageHeight() {
        return (float) height;
    }
}
