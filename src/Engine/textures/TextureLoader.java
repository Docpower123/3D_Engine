package com.example.Engine.textures;

import static org.lwjgl.opengl.GL11.*;
// import static org.lwjgl.opengl.GL12.*;

import org.lwjgl.stb.STBImage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.BufferUtils;

public class TextureLoader {

    private static ByteBuffer readByteBufferFromFileInputStream(FileInputStream fs) throws IOException {
        FileChannel fc = fs.getChannel();
        ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        fc.close();
        fs.close();
        return buffer;
    }

    public static Texture getTexture(String extension, FileInputStream fs) throws IOException {
        Texture texture = new Texture();
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer components = BufferUtils.createIntBuffer(1);
        ByteBuffer buffer = readByteBufferFromFileInputStream(fs);


        ByteBuffer data = STBImage.stbi_load_from_memory(buffer, width, height, components, 4);

        int textureID = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        STBImage.stbi_image_free(data);

        texture.setTextureID(textureID);

        return texture;
    }
}
