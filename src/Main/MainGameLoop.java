package Main;

import Engine.Obj_Loader;
import Engine.entities.Light;
import Engine.Models.RawModel;
import Engine.Models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import Engine.DisplayManager;
import Engine.Loader;
import Engine.Renderer;
import Engine.shaders.StaticShader;
import Engine.Textures.ModelTexture;
import Engine.entities.Camera;
import Engine.entities.Entity;

import java.io.FileNotFoundException;

public class MainGameLoop {

	public static void main(String[] args) throws FileNotFoundException {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		StaticShader shader = new StaticShader();
		Renderer renderer = new Renderer(shader);
		
		RawModel model = Obj_Loader.loadObjModel("dragon", loader);

		TexturedModel staticModel = new TexturedModel(model,new ModelTexture(loader.loadTexture("white")));
		ModelTexture texture = staticModel.getTexture();
		texture.setShineDamper(10);
		texture.setReflectivity(1);

		Entity entity = new Entity(staticModel, new Vector3f(0,0,-25),0,0,0,1);
		Light light = new Light((new Vector3f(0,0,-20)), new Vector3f(1,1,1));
		Camera camera = new Camera();
		
		while(!Display.isCloseRequested()){
			entity.increaseRotation(0, 1, 0);
			camera.move();
			renderer.prepare();
			shader.start();
			shader.loadLight(light);
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
