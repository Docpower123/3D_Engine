package engineTester;
// example of new obj loader class
//ModelData data = OBJFileLoader.loadOBJ("tree");

//RawModel treeModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import entities.Camera;
import entities.Entity;
import entities.Light;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.ModelData;
import toolbox.OBJFileLoader;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();

		//**********Terrain Texture Loading**********
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture,gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		//******************************************

		TexturedModel staticModel = new TexturedModel(OBJLoader.loadObjModel("tree", loader), new ModelTexture(loader.loadTexture("tree")));

		TexturedModel grass = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUsefakeLighting(true);
		TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), new ModelTexture(loader.loadTexture("fern")));
		fern.getTexture().setHasTransparency(true);
		fern.getTexture().setnumberOfRows(2);
		TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), new ModelTexture(loader.loadTexture("lamp")));

		Terrain terrain = new Terrain(0, 0, loader, texturePack, blendMap, "heightmap");


		// Lights loading
		List<Light> lights = new ArrayList<>();
		lights.add(new Light(new Vector3f(0, 1000, -7000), new Vector3f(0.4f, 0.4f, 0.4f)));
		lights.add(new Light(new Vector3f(185, 10, 293), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(370, 17, 300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(293, 7, 305), new Vector3f(2, 2, 0), new Vector3f(1, 0.01f, 0.002f)));

		List<Entity> entities = new ArrayList<>();
		Random random = new Random(676452);
		for (int i=0; i<400; i++){
			if(i % 20 == 0){
				float x = random.nextFloat() * 800;
				float z = random.nextFloat() * 600;
				float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x,y,z),0, random.nextFloat() * 360, 0, 0.9f ));
			}
			if(i % 5 == 0){
				float x = random.nextFloat() * 800;
				float z = random.nextFloat() * 600;
				float y = terrain.getHeightOfTerrain(x,z);
				entities.add(new Entity(grass, new Vector3f(x,y,z),0, random.nextFloat() * 360, 0, random.nextFloat() * 0.1f + 0.6f));
				 x = random.nextFloat() * 800;
				 z = random.nextFloat() * 600;
				 y = terrain.getHeightOfTerrain(x, z);
				 entities.add(new Entity(staticModel, new Vector3f(x,y,z),0, 0, 0, random.nextFloat() * 1 +4 ));
			}
		}
		entities.add(new Entity(lamp, new Vector3f(185, -4.7f, 293), 0,0,0,1));
		entities.add(new Entity(lamp, new Vector3f(370, 4.2f, 300), 0,0,0,1));
		entities.add(new Entity(lamp, new Vector3f(293, -6.8f, 305), 0,0,0,1));

		MasterRenderer renderer = new MasterRenderer(loader);
		RawModel bunny = OBJLoader.loadObjModel("bunny", loader);
		TexturedModel bunnyText = new TexturedModel(bunny, new ModelTexture(loader.loadTexture("white")));
		Player player = new Player(bunnyText, new Vector3f(185, -4.7f, 293), 0,0,0,0.25f);
		Camera camera = new Camera(player);

		List<GuiTexture> guis = new ArrayList<>();
		GuiTexture gui = new GuiTexture(loader.loadTexture("health"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
		guis.add(gui);

		GuiRenderer guiRenderer = new GuiRenderer(loader);


		while(!Display.isCloseRequested()){
			camera.move();
			player.move(terrain);
			renderer.processEntity(player);
			renderer.processTerrain(terrain);
			for(Entity entity:entities){
				renderer.processEntity(entity);
			}
			renderer.render(lights, camera);
			guiRenderer.render(guis);
			DisplayManager.updateDisplay();
		}

		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
