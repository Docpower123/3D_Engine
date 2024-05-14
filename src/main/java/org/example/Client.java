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
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client{

    // Engine Variables
    static Loader loader = new Loader();
    static List<Light> lights = new ArrayList<Light>();
    static List<Entity> entities = new ArrayList<>();
    static List<Enemy> enemies = new ArrayList<>();
    static Map<String, other_players> ips = new HashMap<>();
    private static CountDownLatch latch = new CountDownLatch(1);
    static String ip = "192.168.1.164";
    static int port = 5005;
    static int health_count = 1;
    static List<GuiTexture> guiTextures = new ArrayList<>();

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
        // create lights array and the sun
        Sky sky = new Sky(0.57f, 0.8f, 1.0f, 0.00015f, 7f);
        lights.add(new Light(new Vector3f(30000, 3000, 0), new Vector3f(1, 1, 1)));
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

        // load the gui
        List<GuiTexture> guiTextures = new ArrayList<>();
        GuiRenderer guiRenderer = new GuiRenderer(loader);
        int lastHp = 100;

        // Main Game Loop
        while (!Display_Manager.isCloseRequested()){
            player.move(world, enemies);
            camera.move();

            int currentHealth = player.getHealth();
            // handle clients positions
            client.sendPlayerPosition(player.getPosition(), currentHealth);
            // prints the locations for testings
            Map<String, Vector3f> locations = client.getPlayerPositions();
            locations.forEach((playerId, position) -> {
                if(!ips.containsKey(playerId)){
                    other_players other_player = new other_players(playerModel, position, 3.6f);
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
                //attack feature
                    });
            if(player.getHealth() <= 0){
                break;
            }

            GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
            renderer.renderScene(entities, terrains, lights, sky, camera, new Vector4f(0, -1, 0, 1000000));
            guiRenderer.render(guiTextures);
            Display_Manager.updateDisplay();
        }
        client.sendPlayerPosition(player.getPosition(), 0); // kill this player in others clients
        guiRenderer.cleanUp();
        client.stopClient();
        renderer.cleanUp();
        loader.cleanUp();
        Display_Manager.closeDisplay();
    }
}