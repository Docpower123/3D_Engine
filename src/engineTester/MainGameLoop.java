package engineTester;
// example of new obj loader class
//ModelData data = OBJFileLoader.loadOBJ("tree");

//RawModel treeModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entities.Player;
import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.Display;
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

	public static void main(String[] args) throws IOException {

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

		Terrain terrain = new Terrain(0, 0, loader, texturePack, blendMap, "heightmap");
		Light light = new Light(new Vector3f(0,1000000,0),new Vector3f(1,1,1));

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

		MasterRenderer renderer = new MasterRenderer();
		RawModel bunny = OBJLoader.loadObjModel("bunny", loader);
		TexturedModel bunnyText = new TexturedModel(bunny, new ModelTexture(loader.loadTexture("white")));
		Player player = new Player(bunnyText, new Vector3f(100,0,50), 0,0,0,0.25f);
		Camera camera = new Camera(player);

		while(!Display.isCloseRequested()){
			camera.move();
			player.move(terrain);
			renderer.processEntity(player);
			renderer.processTerrain(terrain);
			for(Entity entity:entities){
				renderer.processEntity(entity);
			}
			renderer.render(light, camera);
			DisplayManager.updateDisplay();
		}

		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}

}
