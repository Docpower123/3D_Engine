package engine.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.io.IOException;

public class Material {
	private String path;
	private Texture texture;
	private float width, height;
	private int textureID;

	public Material(String path) {
		this.path = path;
	}

	public void create() {
		try {
			// Adjust the path to start from the root of the resources directory
			String fullPath = "/textures/" + path;
			// Load the texture using the corrected path
			texture = TextureLoader.getTexture(Material.class.getResourceAsStream(fullPath));
			if (texture != null) {
				width = texture.getWidth();
				height = texture.getHeight();
				textureID = texture.getTextureID();
			} else {
				System.err.println("Failed to load texture from path: " + fullPath);
			}
		} catch (IOException e) {
			System.err.println("Failed to load texture from path: " + path);
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("Texture file not found at path: " + path);
			e.printStackTrace();
		}
	}

	public void destroy() {
		GL13.glDeleteTextures(textureID);
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public int getTextureID() {
		return textureID;
	}
}
