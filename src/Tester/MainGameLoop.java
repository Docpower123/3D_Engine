package Tester;

import Shaders.StaticShader;
import models.TextureModel;
import renderEngine.Display_Manager;
import renderEngine.Loader;
import renderEngine.RawModel;
import renderEngine.Renderer;

public class MainGameLoop {

    public static void main(String[] args) {
        Display_Manager displayManager = new Display_Manager();

        // Create a display
        displayManager.createDisplay();

        Loader loader = new Loader();
        Renderer renderer = new Renderer();
        StaticShader shader = new StaticShader();

        float[] vertices = {
                -0.5f, 0.5f, 0f,
                -0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                0.5f, 0.5f, 0f,
        };

        int[] indices = {
                0, 1, 3,
                3, 1, 2
        };

// Define texture coordinates for a rectangle
        float[] textureCoords = {
                1, 0,   // Top-left
                0.5f, 0.5f,   // Bottom-left
                1, 1,   // Bottom-right
                1, 0    // Top-right
        };

        RawModel model = loader.loadToVAO(vertices, textureCoords, indices);

        // Load texture
        int textureID = loader.loadTexture("C:\\Users\\yuval\\IdeaProjects\\game\\src\\cat.png");

        // Combine model and texture into a TextureModel
        TextureModel texturedModel = new TextureModel(model, textureID);

        // Main loop
        while (!displayManager.shouldClose()) {
            // Game logic

            // Render
            renderer.prepare();
            shader.start();
            renderer.render(texturedModel); // Render the textured model
            shader.stop();
            displayManager.updateDisplay();
        }

        // Clean up
        shader.cleanUp();
        loader.cleanUp();
        displayManager.closeDisplay();
    }
}
