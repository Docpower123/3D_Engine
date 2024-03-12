package engine.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TextureLoader {
    private static Map<String, Texture> loadedTextures = new HashMap<>();

    public static Texture getTexture(InputStream inputStream) throws IOException {
        // Create a unique format identifier based on the input stream
        String format = inputStream.toString(); // You might want to use a more reliable method to generate the format

        // Check if the texture has already been loaded
        Texture texture = loadedTextures.get(format);
        if (texture != null) {
            return texture;
        }

        try {
            // Load the texture using a custom method or another library
            // Replace the following line with the actual texture loading logic
            // For example, you could use STB Image or another image loading library
            int textureID = loadTextureFromInputStream(inputStream);

            // Create a Texture object and add it to the map
            int width = 100;
            int height = 100;
            texture = new Texture(textureID, width, height);
            loadedTextures.put(format, texture);
            return texture;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static int loadTextureFromInputStream(InputStream inputStream) throws IOException {
        // Implement the logic to load the texture from the input stream
        // Return the OpenGL texture ID
        // This is just a placeholder method, you need to replace it with actual texture loading logic
        throw new UnsupportedOperationException("Texture loading logic not implemented");
    }

    public static void destroyTextures() {
        for (Texture texture : loadedTextures.values()) {
            GL13.glDeleteTextures(texture.getTextureID());
        }
        loadedTextures.clear();
    }
}
