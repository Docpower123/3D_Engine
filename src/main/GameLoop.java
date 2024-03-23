package Main;

import Engine.Display_Manager;
import Engine.RawModel;
import Shaders.Static_Shader;
import org.lwjgl.opengl.Display;

import Engine.Loader;
import Engine.Renderer;

public class GameLoop {

    public static void main(String[] args){

        Display_Manager.create();

        Loader loader = new Loader();
        Renderer renderer = new Renderer();
        Static_Shader shader = new Static_Shader();

        float[] vertices = {
                // Left bottom triangle
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

        while (!Display.isCloseRequested()){
            renderer.prepare();
            shader.start();
            renderer.render(model);
            shader.stop();
            Display_Manager.update();
        }

        shader.cleanUp();
        loader.cleanUP();
        Display_Manager.close();
    }

}
