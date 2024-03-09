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
                0,1,3,
                3,1,2
        };

        RawModel model = loader.loadToVAO(vertices, indices);

        // Load texture
        int textureID = loader.loadTexture("cat.png");
        TextureModel texture = new TextureModel(textureID);

        // Combine model and texture into a TextureModel
        TextureModel texturedModel = new TextureModel(model, texture.getTextureID());

        // Main loop
        while (!displayManager.shouldClose()) {
            // game logic
            renderer.prepare();
            shader.start();
            renderer.render(texturedModel); // Render the textured model
            shader.stop();
            displayManager.updateDisplay();
        }

        shader.cleanUp();
        loader.cleanUp();
        displayManager.closeDisplay();
    }
}
