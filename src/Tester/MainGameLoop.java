package Tester;

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

        float[] vertices = {
                // Left bottom triangle
                -0.5f, 0.5f, 0f,
                -0.5f, -0.5f, 0f,
                0.5f, -0.5f, 0f,
                // Right top triangle
                0.5f, -0.5f, 0f,
                0.5f, 0.5f, 0f,
                -0.5f, 0.5f, 0f
        };

        RawModel model = loader.loadToVAO(vertices);

        // Main loop
        while (!displayManager.shouldClose()) {
            // game logic
            renderer.prepare();
            renderer.render(model);
            displayManager.updateDisplay();
        }

        loader.cleanUp();
        displayManager.closeDisplay();
    }
}
