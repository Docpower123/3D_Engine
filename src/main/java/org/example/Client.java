package org.example;



import org.example.Engine.Display_Manager;
import org.example.Engine.Loader;
import org.example.Engine.MasterRenderer;
import org.example.Engine.entities.*;
import org.example.Engine.guis.GuiRenderer;
import org.example.Engine.models.TexturedModel;
import org.example.Engine.skybox.Sky;
import org.example.Engine.terrains.GameWorld;
import org.example.Engine.terrains.Terrain;
import org.example.Engine.terrains.World;
import org.example.Engine.toolbox.MousePicker;
import org.example.Engine.water.*;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Client{

    // Networking Variables
    private static CountDownLatch latch;
    private static volatile String[] world_packet;
    private static Socket client; // Client socket
    private static volatile Map<String, Vector3f> loc_map = new HashMap<>();
    private static volatile Map<String, other_players> players_map = new HashMap<>();
    private static volatile List<String> ips = new ArrayList<>();

    // Game Variables
    static String title = "3D Engine Demo";
    static Loader loader = new Loader();
    static Random random = new Random(676452);
    static List<Entity> entities = new ArrayList<>();
    static List<Enemy> enemies = new ArrayList<>();
    static List<other_players> players = new ArrayList<>();


    static class Networking implements Runnable {
        private OutputStream outputStream; // Output stream for sending data

        public void send(String data) throws IOException {
            outputStream.write(data.getBytes());
            outputStream.flush();
        }

        @Override
        public void run() {
            String ip = "192.168.1.190";
            int port = 12345;
            try {
                client = new Socket(ip, port);
                outputStream = client.getOutputStream(); // Initialize the output stream
                boolean is_first = false;
                String line;
                InputStream data = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(data));
                while (true) { // Check if thread is not interrupted
                    if (!is_first && (line = reader.readLine()) != null) {
                        world_packet = line.substring(1, line.length() - 1).split(", ");
                        is_first = true;
                        latch.countDown(); // Signal that the packet is received
                    } else {
                        if ((line = reader.readLine()) != null) {
                            String[] data_packet = line.substring(3, line.length() - 1).split(":");
                            System.out.println(data_packet[1]);
                            String ip_pl = data_packet[0].split(",")[0];
                            ip_pl = ip_pl.substring(0, ip_pl.length() - 1);
                            // format location out of the packet
                            Float x,y,z;
                            String[] location = data_packet[1].substring(2, data_packet[1].length() - 1).split(",");
                            x = Float.parseFloat(location[0]);
                            y = Float.parseFloat(location[1]);
                            String[] data_z = location[2].split(",");
                            String z_string = data_z[0];
                            z = Float.parseFloat(z_string.split("/n")[0]);
                            System.out.println(z);
                            loc_map.put(ip_pl, new Vector3f(x, y, z));
                            ips.add(ip_pl);

                        } else {
                            // Handle the case where the server closes the connection
                            System.out.println("Server closed the connection.");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection reset by server.");
            }
        }

        public void interrupt() {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Display_Manager.createDisplay(title);
        MasterRenderer renderer = new MasterRenderer(loader);
        latch = new CountDownLatch(1); // Initialize the latch
        Networking networking = new Networking();
        Thread thread = new Thread(networking);
        thread.start();
        latch.await(); // Wait until the world packet is received
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
        for(int i=2; i<200; i+=2){
            ex = Float.parseFloat(world_packet[i]);
            ez = Float.parseFloat(world_packet[i+1]);
            ey = world.getHeightOfTerrain(ex, ez);
            entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0,0,0, 1f));
            lights.add(new Light(new Vector3f(ex, ey+14,ez), new Vector3f(3,1,1), new Vector3f(1, 0.001f, 0.002f)));
        }

        // loop to read all entities positions and add them to the list
        for(int i=200; i<50000; i+=6){
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
        while(!Display_Manager.isCloseRequested()){
            if(ips != null) {
                for (int i = 0; i < ips.size(); i++) {
                    if (players_map.containsKey(ips.get(i))) {
                        players_map.get(ips.get(i)).moving(loc_map.get(ips.get(i)));
                    } else {
                        other_players player1 = new other_players(playerModel, loc_map.get(ips.get(i)), 1.6f);
                        players_map.put(ips.get(i), player1);
                        entities.add(player1);
                    }
                }
            }
            player.move(world, enemies);
            // send player location to server
            if(networking != null){
                networking.send(player.getPosition().x +","+ player.getPosition().y +","+ player.getPosition().z+"/n");
            }
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
            Display_Manager.updateDisplay();
        }

        //TODO: send kill signal to server
        networking.interrupt(); // Interrupt the networking thread
        buffers.cleanUp();
        waterShader.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        Display_Manager.closeDisplay();
    }
}