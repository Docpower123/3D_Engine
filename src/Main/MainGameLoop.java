package Main;

import Engine.Obj_Loader;
import Engine.models.RawModel;
import Engine.models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import Engine.DisplayManager;
import Engine.Loader;
import Engine.Renderer;
import Engine.shaders.StaticShader;
import Engine.textures.ModelTexture;
import Engine.entities.Camera;
import Engine.entities.Entity;

import java.io.FileNotFoundException;

public class MainGameLoop {

	public static void main(String[] args) throws FileNotFoundException {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);
		
		RawModel model = Obj_Loader.loadObjModel("stall", loader);

		TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("stallTexture")));
		
		Entity entity = new Entity(staticModel, new Vector3f(0,0,-50),0,0,0,1);
		
		Camera camera = new Camera();
		
		while(!Display.isCloseRequested()){
			entity.increaseRotation(0, 1, 0);
			camera.move();
			renderer.prepare();
			shader.start();
			shader.loadViewMatrix(camera);
			renderer.render(entity,shader);
			shader.stop();
			DisplayManager.updateDisplay();
		}

		shader.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
