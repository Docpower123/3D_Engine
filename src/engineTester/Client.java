package com.example.engineTester;

import com.example.Engine.entities.*;
import com.example.Engine.guis.GuiRenderer;
import com.example.Engine.models.TexturedModel;
import com.example.Engine.renderEngine.DisplayManager;
import com.example.Engine.renderEngine.Loader;
import com.example.Engine.renderEngine.MasterRenderer;
import com.example.Engine.skybox.Sky;
import com.example.Engine.terrains.GameWorld;
import com.example.Engine.terrains.Terrain;
import com.example.Engine.terrains.World;
import com.example.Engine.toolbox.MousePicker;
import com.example.Engine.water.*;
import org.joml.Vector3f;
import java.util.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client{
    static String title = "3D Engine Demo";
    static Loader loader = new Loader();
    static Random random = new Random(676452);
    static List<Entity> entities = new ArrayList<>();


    // add code to establish connection and receive world packet


    public static String[] wifi(){
        String[] world_packet = {"20000", "20000", "rocksModel", "2", "1"};
        return world_packet;
    }

    public static void main(String[] args){
        DisplayManager.createDisplay(title);
        MasterRenderer renderer = new MasterRenderer(loader);
        String[] world_packet = wifi();
        World world = new GameWorld(loader,Float.parseFloat(world_packet[0]), Float.parseFloat(world_packet[1]), 0);
        List<Terrain> terrains = world.getTerrains();

        // Load all Models
        TexturedModel treeModel = loader.createTexturedModel("tree", "tree", 1, 0);
        TexturedModel lowPolyTreeModel = loader.createTexturedModel("lowPolyTree", "lowPolyTree4", 2, 1, 0, false, false);
        TexturedModel pineModel = loader.createTexturedModel("pine", "pine", 10, 0.5f);
        TexturedModel grassModel = loader.createTexturedModel("grassModel", "grassTexture", 1, 0, true, true);
        TexturedModel flowerModel = loader.createTexturedModel("grassModel", "flower", 1, 0, true, true);
        TexturedModel fernModel = loader.createTexturedModel("fern", "fern4", 2, 1, 0, true, false);
        TexturedModel rocksModel = loader.createTexturedModel("rocks", "rocks", 10, 1);
        TexturedModel toonRocksModel = loader.createTexturedModel("toonRocks", "toonRocks", 10, 1);
        TexturedModel lampModel = loader.createTexturedModel("lamp", "lamp", 1, 0, false, true);

        // TODO: add all model to a hash map
        Map<String, Object> variables = new HashMap<>();
        variables.put("rocksModel", rocksModel);

        float ex, ey, ez, rx, ry, rz, scale;
        // create lights array and the sun
        Sky sky = new Sky(0.57f, 0.8f, 1.0f, 0.00015f, 7f);
        List<Light> lights = new ArrayList<Light>();
        lights.add(new Light(new Vector3f(30000, 3000, 0), new Vector3f(1, 1, 1)));

        // loop to read all lamps positions and add them to the list
        for(int i=3; i<100; i+=3){
            ex = Float.parseFloat(world_packet[i+1]);
            ez = Float.parseFloat(world_packet[i+2]);
            ey = world.getHeightOfTerrain(ex, ez);
            entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0,0,0, 1f));
            lights.add(new Light(new Vector3f(ex, ey+14,ez), new Vector3f(3,1,1), new Vector3f(1, 0.001f, 0.002f)));
        }

        // loop to read all entities positions and add them to the list
        for(int i=100; i<2100; i+=6){
            TexturedModel model = (TexturedModel) variables.get(world_packet[i]);
            ex = Float.parseFloat(world_packet[i+1]);
            ez = Float.parseFloat(world_packet[i+2]);
            ey = world.getHeightOfTerrain(ex, ez);
            rx = Float.parseFloat(world_packet[i+3]);
            rz = Float.parseFloat(world_packet[i+4]);
            scale = Float.parseFloat(world_packet[i+5]);
            int numTextureRows = model.getTexture().getNumberOfRows();
            int numSubTextures = numTextureRows * numTextureRows;

            if(ey > world.getHeightOfWater(ex, ez)){
                ry = ey * 360;

                if(numSubTextures > 1){
                    //TODO: change it to be delivered in the packet so there wont be any different textures
                    int textureIndex = random.nextInt(numSubTextures);
                    entities.add(new Entity(model, textureIndex, new Vector3f(ex,ey,ez), rx, ry, rz ,scale));
                }
                else{
                    entities.add(new Entity(model, new Vector3f(ex,ey,ez), rx, ry, rz ,scale));
                }
            }
        }

        // set up the player & camera
        float player_x = random.nextFloat();
        float player_z = random.nextFloat();
        float player_y = world.getHeightOfTerrain(player_x, player_z);
        TexturedModel playerModel = loader.createTexturedModel("bunny", "white", 1, 0);
        Player player = new Player(playerModel, new Vector3f(player_x, player_y, player_z), 0, 2, 0, 0.6f);
        entities.add(player);

        Camera camera1 = new GameCamera(player);
        camera1.getPosition().set(0, 20, 0);

        Camera camera2 = new PlayerCamera();
        camera2.getPosition().set(0, 30, 0);

        Camera camera = camera1;

        // set up water
        WaterFrameBuffers buffers = new WaterFrameBuffers();

        WaterShader waterShader = new WaterShader();
        WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
        List<WaterTile> waters = new ArrayList<>();
        WaterTile water = new GameWaterTile(0, 0, 0, Float.parseFloat(world_packet[0]));
        waters.add(water);
        water = new GameWaterTile(-1 *  Float.parseFloat(world_packet[0]), 0, 0,  Float.parseFloat(world_packet[0]));
        waters.add(water);
        water = new GameWaterTile(-1 *  Float.parseFloat(world_packet[0]), -1 *  Float.parseFloat(world_packet[0]), 0,  Float.parseFloat(world_packet[0]));
        waters.add(water);
        water = new GameWaterTile(0, -1 *  Float.parseFloat(world_packet[0]), 0,  Float.parseFloat(world_packet[0]));
        waters.add(water);


        GuiRenderer guiRenderer = new GuiRenderer(loader);

        MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), world);





    }


}