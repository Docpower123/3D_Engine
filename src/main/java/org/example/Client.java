package org.example;

import org.example.Engine.Display_Manager;
import org.example.Engine.Loader;
import org.example.Engine.MasterRenderer;
import org.example.Engine.entities.*;
import org.example.Engine.guis.GuiRenderer;
import org.example.Engine.guis.GuiTexture;
import org.example.Engine.models.TexturedModel;
import org.example.Engine.skybox.Sky;
import org.example.Engine.terrains.GameWorld;
import org.example.Engine.terrains.Terrain;
import org.example.Engine.terrains.World;
import org.example.Engine.water.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;


import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Client{

    // Engine Variables
    static Loader loader = new Loader();
    static List<Light> lights = new ArrayList<Light>();
    static List<Entity> entities = new ArrayList<>();
    static List<Enemy> enemies = new ArrayList<>();
    static Map<String, other_players> ips = new HashMap<>();
    static String ip = "localhost";
    static int port = 5005;
    static Random random = new Random(676452);


    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        GameClient client = new GameClient(ip, port);
        // Set up and run the Client
        new Thread(client).start();

        // Engine Set up
        Display_Manager.createDisplay("3D Game Engine");
        MasterRenderer renderer = new MasterRenderer(loader);
        // Wait for the world data to come from the server and parse it
        while (client.getWorldData() == null){Thread.sleep(100);}
        String[] world_data = client.getWorldData().substring(1, client.getWorldData().length()-2).split(",");
        World world = new GameWorld(loader, Float.parseFloat(world_data[0]), Float.parseFloat(world_data[1]), 0);
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

        Map<String, Object> variables = new HashMap<>();
        variables.put(" 'rocksModel'", rocksModel);
        variables.put(" 'toonRocksModel'", toonRocksModel);
        variables.put(" 'treeModel'", treeModel);
        variables.put(" 'lowPolyTreeModel'", lowPolyTreeModel);
        variables.put(" 'pineModel'", pineModel);
        variables.put(" 'grassModel'", grassModel);
        variables.put(" 'flowerModel'", flowerModel);
        variables.put(" 'fernModel'", fernModel);

        // create lights array and the sun
        Sky sky = new Sky(0.57f, 0.8f, 1.0f, 0.00015f, 7f); // the color of the sky
        lights.add(new Light(new Vector3f(30000, 3000, 0), new Vector3f(1, 1, 1)));
        float ex, ey, ez, rx, ry, rz, scale;
        ex = 100;
        ez = 100;
        ey = world.getHeightOfTerrain(ex, ez);
        entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0,0,0, 10f));
        lights.add(new Light(new Vector3f(100, ey+140,100), new Vector3f(3,1,1), new Vector3f(1, 0.001f, 0.002f)));


        // loop to read all lamps positions and add them to the list
        for(int i=2; i<200; i+=2){
            ex = Float.parseFloat(world_data[i]);
            ez = Float.parseFloat(world_data[i+1]);
            ey = world.getHeightOfTerrain(ex, ez);
            entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0,0,0, 1f));
            lights.add(new Light(new Vector3f(ex*10, ey+140,ez*10), new Vector3f(3,1,1), new Vector3f(1, 0.001f, 0.002f)));
        }

        // loop to read all entities positions and add them to the list
        for(int i=200; i<50000; i+=6){
            TexturedModel model = (TexturedModel) variables.get(world_data[i]);
            ex = Float.parseFloat(world_data[i+1]);
            ez = Float.parseFloat(world_data[i+2]);
            ey = world.getHeightOfTerrain(ex, ez);
            rx = Float.parseFloat(world_data[i+3]);
            rz = Float.parseFloat(world_data[i+4]);
            scale = Float.parseFloat(world_data[i+5]);
            int numTextureRows = model.getTexture().getNumberOfRows();
            int numSubTextures = numTextureRows * numTextureRows;

            if(ey > world.getHeightOfWater(ex, ez)){
                ry = ey * 360;

                if(numSubTextures > 1){
                    int textureIndex = random.nextInt(numSubTextures);
                    entities.add(new Entity(model, textureIndex, new Vector3f(ex,ey,ez), rx, ry, rz ,scale));
                }
                else{
                    entities.add(new Entity(model, new Vector3f(ex,ey,ez), rx, ry, rz ,scale));
                }
            }
        }


        // set up the player & camera
        float player_x = 20f;
        float player_z = 31f;
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
        WaterTile water = new GameWaterTile(0, 0, 0, Float.parseFloat(world_data[0]));
        waters.add(water);
        water = new GameWaterTile(-1 *  Float.parseFloat(world_data[0]), 0, 0,  Float.parseFloat(world_data[0]));
        waters.add(water);
        water = new GameWaterTile(-1 *  Float.parseFloat(world_data[0]), -1 *  Float.parseFloat(world_data[0]), 0,  Float.parseFloat(world_data[0]));
        waters.add(water);
        water = new GameWaterTile(0, -1 *  Float.parseFloat(world_data[0]), 0,  Float.parseFloat(world_data[0]));
        waters.add(water);


        // load the gui
        List<GuiTexture> guiTextures = new ArrayList<>();
        GuiRenderer guiRenderer = new GuiRenderer(loader);
        int lastHp = 100;
        client.sendPlayerPosition(player.getPosition(), lastHp, player.getAttack(), false);
        String addr = client.getip();


        // Main Game Loop
        while (!Display_Manager.isCloseRequested()){
            if(client.getwin()){
                break;
            }
            player.move(world, enemies);
            camera.move();
            for (Map.Entry<String, Integer> entry : client.getPlayerhealth().entrySet()) {
                String key = entry.getKey();
                int hp = entry.getValue();
                key = key.split(",")[0].substring(2, key.split(",")[0].length()-1);
                if(addr.equals(key)){
                    player.sethp(hp);
                    System.out.println(hp);
                }
            }
            int currentHealth = player.getHealth();
            // handle clients positions
            client.sendPlayerPosition(player.getPosition(), currentHealth, player.getAttack(), false);
            player.setAttack(false);
            // prints the locations for testings
            Map<String, Vector3f> locations = client.getPlayerPositions();
            locations.forEach((playerId, position) -> {
                System.out.println(playerId);
                if(!ips.containsKey(playerId)){
                    other_players other_player = new other_players(playerModel, position, 0.6f);
                    ips.put(playerId, other_player);
                    entities.add(other_player);
                }
                ips.get(playerId).moving(position);
            });

            // handle hp
            Map<String, Integer> healths = client.getPlayerhealth();
            healths.forEach((playerId, health) -> {
                // if hp = 0 remove entity
                if(health <= 0){
                    entities.remove(ips.get(playerId));
                    ips.remove(playerId);
                }
                    });
            if(player.getHealth() <= 0){
                break;
            }

            if(client.isKilled()){
                break;
            }

            float targetX = 100;
            float targetZ = 100;
            float radius = 20;

            if(Math.sqrt(Math.pow(player.getPosition().x - targetX, 2) + Math.pow(player.getPosition().z - targetZ, 2)) <= radius) {
                GuiTexture gui3 = new GuiTexture(loader.loadTexture("winer"), new Vector2f(0.05f, 0.1f), new Vector2f(0.5f, 0.5f));
                guiTextures.add(gui3);
                guiRenderer.render(guiTextures);
                Display_Manager.updateDisplay();
                client.sendPlayerPosition(player.getPosition(),100,false,true);
                Thread.sleep(2000);
                break;
            }
            if(client.getKilledPlayerId() != null){
                entities.remove(ips.get(client.getKilledPlayerId()));
                ips.remove(client.getKilledPlayerId());
            }

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
            Display_Manager.updateDisplay();

        }
        client.sendPlayerPosition(player.getPosition(), 0, player.getAttack(), false); // kill this player in others clients
        guiRenderer.cleanUp();
        client.stopClient();
        renderer.cleanUp();
        loader.cleanUp();
        Display_Manager.closeDisplay();
    }
}