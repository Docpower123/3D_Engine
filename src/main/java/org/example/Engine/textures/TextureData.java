package org.example.Engine.textures;

import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

public class TextureData {

    private int width;
    private int height;
    private ByteBuffer buffer;

    public TextureData(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }
    public void freeBuffer() {
        STBImage.stbi_image_free(buffer);
        buffer = null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
