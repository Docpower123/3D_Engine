package com.example.engineTester;

import com.example.Engine.entities.*;
import com.example.Engine.guis.GuiRenderer;
import com.example.Engine.models.TexturedModel;
import com.example.Engine.renderEngine.Display;
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
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
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
    static List<Enemy> enemies = new ArrayList<>();

    public static String[] wifi() throws IOException {
        String ip = "localhost";
        int port = 12345;
        Socket client = new Socket(ip, port);
        InputStream data = client.getInputStream();

        // Create a BufferedReader to read data from InputStream
        BufferedReader reader = new BufferedReader(new InputStreamReader(data));

        // Read and print data from the server in a while loop
        String line;
        boolean flag = false;
        String[] world_packet = null;
        while ((line = reader.readLine()) != null) {
            if (!flag) {
                flag = true;
                System.out.println(STR."World Packet: \{line}");
                world_packet = line.substring(1, line.length() - 1).split(", ");
                System.out.println(world_packet.length);
                System.out.println(world_packet[99]);
                System.out.println(world_packet[100]);
            } else {
                System.out.println(line);
            }
        }

        // Close the resources
        reader.close();
        client.close();
        return world_packet;
    }

    public static void main(String[] args) throws IOException {
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
        variables.put("'rocksModel'", rocksModel);
        variables.put("'toonRocksModel'", toonRocksModel);
        variables.put("'treeModel'", treeModel);
        variables.put("'lowPolyTreeModel'", lowPolyTreeModel);
        variables.put("'pineModel'", pineModel);
        variables.put("'grassModel'", grassModel);
        variables.put("'flowerModel'", flowerModel);
        variables.put("'fernModel'", fernModel);

        float ex, ey, ez, rx, ry, rz, scale;
        // create lights array and the sun
        Sky sky = new Sky(0.57f, 0.8f, 1.0f, 0.00015f, 7f);
        List<Light> lights = new ArrayList<Light>();
        lights.add(new Light(new Vector3f(30000, 3000, 0), new Vector3f(1, 1, 1)));

        // loop to read all lamps positions and add them to the list
        for(int i=2; i<4; i+=2){
            ex = Float.parseFloat(world_packet[i]);
            ez = Float.parseFloat(world_packet[i+1]);
            ey = world.getHeightOfTerrain(ex, ez);
            entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0,0,0, 1f));
            lights.add(new Light(new Vector3f(ex, ey+14,ez), new Vector3f(3,1,1), new Vector3f(1, 0.001f, 0.002f)));
        }

        // loop to read all entities positions and add them to the list
        for(int i=100; i<106; i+=6){
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

        // main game loop
        while(!Display.isCloseRequested()){
            player.move(world, enemies);
            camera.move();
            picker.update();
            GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
            // render to reflection texture: set the clip plane to clip stuff above water
            buffers.bindReflectionFrameBuffer();
            float distance = 2 * (camera.getPosition().y - water.getHeight());
            // change position and pitch of camera to render the reflection
            camera.getPosition().y -= distance;
            camera.invertPitch();
            renderer.renderScene(entities, terrains, lights, sky, camera, new Vector4f(0, 1, 0, -water.getHeight()+1f));
            camera.getPosition().y += distance;
            camera.invertPitch();

            // render to refraction texture: set the clip plane to clip stuff below water
            buffers.bindRefractionFrameBuffer();
            renderer.renderScene(entities, terrains, lights, sky, camera, new Vector4f(0, -1, 0, water.getHeight()+1f));

            // render to screen: set the clip plane at a great height, so it won't clip anything
            buffers.unbindCurrentFrameBuffer();
            renderer.renderScene(entities, terrains, lights, sky, camera, new Vector4f(0, -1, 0, 1000000));

            waterRenderer.render(waters, sky, camera, lights);
            int frames = DisplayManager.updateDisplay();
        }
        buffers.cleanUp();
        waterShader.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }


}