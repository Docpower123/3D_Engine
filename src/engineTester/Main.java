package com.example.engineTester;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// lwjgl libs
import com.example.Engine.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.example.Engine.entities.*;
import com.example.Engine.guis.GuiRenderer;
import com.example.Engine.guis.GuiTexture;
import com.example.Engine.water.WaterTile;
import com.example.Engine.fontMeshCreator.FontType;
import com.example.Engine.fontMeshCreator.GUIText;
import com.example.Engine.fontRendering.TextMaster;
import com.example.Engine.models.TexturedModel;
import com.example.Engine.renderEngine.Display;
import com.example.Engine.renderEngine.DisplayManager;
import com.example.Engine.renderEngine.Loader;
import com.example.Engine.renderEngine.MasterRenderer;
import com.example.Engine.skybox.Sky;
import com.example.Engine.terrains.Terrain;
import com.example.Engine.terrains.World;
import com.example.Engine.terrains.GameWorld;
import com.example.Engine.toolbox.MousePicker;
import com.example.Engine.water.WaterFrameBuffers;
import com.example.Engine.water.WaterRenderer;
import com.example.Engine.water.WaterShader;
import com.example.Engine.water.GameWaterTile;

public class Main
{
    public static void main(String[] args) {
        new Main();
    }

    String title = "3D Engine";
    float terrainSize = 20000;
    float terrainMaxHeight = 2000;
    float waterSize = terrainSize;
    float waterHeight = 0;
    Random random = new Random(676452);
    int health_count = 1;
    Loader loader = new Loader();
    List<Entity> entities = new ArrayList<>();
    List<Enemy> enemies = new ArrayList<>();
    List<GuiTexture> guiTextures = new ArrayList<>();


    // add all entities with a randomized location to a List of entities
    public void addEntity(World world, TexturedModel texturedModel, float rx, float rz, float scale) {
        int numTextureRows = texturedModel.getTexture().getNumberOfRows();
        int numSubTextures = numTextureRows * numTextureRows;

        float x = random.nextFloat() * terrainSize - terrainSize / 2;
        float z = random.nextFloat() * terrainSize - terrainSize / 2;
        float y = world.getHeightOfTerrain(x, z);
        if (y > world.getHeightOfWater(x, z)) {
            float ry = random.nextFloat() * 360;

            if (numSubTextures > 1) {
                int textureIndex = random.nextInt(numSubTextures);
                entities.add(new Entity(texturedModel, textureIndex, new Vector3f(x, y, z), rx, ry, rz, scale));
            }
            else {
                entities.add(new Entity(texturedModel, new Vector3f(x, y, z), rx, ry, rz, scale));
            }
        }
    }

    public void Update_Health_Gui() {
        if(health_count <= 6){
            GuiTexture gui3 = new GuiTexture(loader.loadTexture("health/h" + health_count), new Vector2f(0.8f, 0.8f), new Vector2f(0.1f, 0.1f));
            guiTextures.add(gui3);
            health_count++;
        }
    }

    public Main() {
        DisplayManager.createDisplay(title);
        MasterRenderer renderer = new MasterRenderer(loader);
        TextMaster.init(loader);
        if (title.length() > 0) {
            FontType font = new FontType(loader.loadFontTextureAtlas("candara"), new File("res/fonts/candara.fnt"));
            GUIText text = new GUIText(title, 1.3f, font, new Vector2f(0.0f, 0.85f), 0.3f, true);
            text.setColor(0.1f, 0.1f, 0.4f);
        }

        World world = new GameWorld(loader, terrainSize, terrainMaxHeight, waterHeight);
        List<Terrain> terrains = world.getTerrains();

        // *****************************************

        TexturedModel treeModel = loader.createTexturedModel("tree", "tree", 1, 0);
        TexturedModel lowPolyTreeModel = loader.createTexturedModel("lowPolyTree", "lowPolyTree4", 2, 1, 0, false, false);
        TexturedModel pineModel = loader.createTexturedModel("pine", "pine", 10, 0.5f);
        TexturedModel grassModel = loader.createTexturedModel("grassModel", "grassTexture", 1, 0, true, true);
        TexturedModel flowerModel = loader.createTexturedModel("grassModel", "flower", 1, 0, true, true);
        TexturedModel fernModel = loader.createTexturedModel("fern", "fern4", 2, 1, 0, true, false);
        TexturedModel rocksModel = loader.createTexturedModel("rocks", "rocks", 10, 1);
        TexturedModel toonRocksModel = loader.createTexturedModel("toonRocks", "toonRocks", 10, 1);
        TexturedModel lampModel = loader.createTexturedModel("lamp", "lamp", 1, 0, false, true);

        float ex, ey, ez;

        entities.add(new Entity(rocksModel, new Vector3f(0, 0, 0), 0, 0, 0, 75));

        ex = -30;
        ez = 220;
        ey = world.getHeightOfTerrain(ex, ez);
        entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0, 0, 0, 1f));

        Sky sky = new Sky(0.57f, 0.8f, 1.0f, 0.00015f, 7f);

        // Sun and Lamps
        List<Light> lights = new ArrayList<Light>();
        lights.add(new Light(new Vector3f(30000, 3000, 0), new Vector3f(0.0039f, 0.0055f, 0.068f)));

        ex = 1126.3969f;
        ez = 2621.307f;
        ey = world.getHeightOfTerrain(ex, ez);
        entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0, 0, 0, 1f));
        lights.add(new Light(new Vector3f(ex, ey+14, ez), new Vector3f(3, 1, 1), new Vector3f(1, 0.01f, 0.002f)));

        ex = 375.8717f;
        ez = 587.5373f;
        ey = world.getHeightOfTerrain(ex, ez);
        //entities.add(new Entity(lampModel, new Vector3f(ex, ey, ez), 0, 0, 0, 1f));
        //lights.add(new Light(new Vector3f(ex, ey+14, ez), new Vector3f(1, 2, 0), new Vector3f(1, 0.01f, 0.002f)));

        ex = 362.69772f;
        ez = 500.70355f;
        ey = world.getHeightOfTerrain(ex, ez);
        Entity lampEntity = new Entity(lampModel, new Vector3f(ex, ey, ez), 0, 0, 0, 1f);
        entities.add(lampEntity);
        Light lampLight = new Light(new Vector3f(ex, ey+14, ez), new Vector3f(3, 5, 8), new Vector3f(0.8f, 0.00001f, 0.01f));
        lights.add(lampLight);

        for (int i = 0; i < 2000; i++) {
            if (i % 3 == 0) {
                addEntity(world, grassModel, 0, 0, 1.8f);
                addEntity(world, flowerModel, 0, 0, 2.3f);
            }

            if (i % 2 == 0) {
                addEntity(world, fernModel, 10 * random.nextFloat() - 5, 10 * random.nextFloat() - 5, 0.9f);

                // low poly tree "bobble"
                addEntity(world, lowPolyTreeModel, 4 * random.nextFloat() - 2, 4 * random.nextFloat() - 2, random.nextFloat() * 0.1f + 0.6f);

                addEntity(world, treeModel,  4 * random.nextFloat() - 2, 4 * random.nextFloat() - 2, random.nextFloat() * 1f + 4f);
                addEntity(world, pineModel,  4 * random.nextFloat() - 2, 4 * random.nextFloat() - 2, random.nextFloat() * 4f + 1f);

                addEntity(world, toonRocksModel, 0, 0, 4 * random.nextFloat());
            }
        }

        float px = 350f; //-2163f;
        float pz = 540f; //2972f;
        float py = world.getHeightOfTerrain(px, pz);

        TexturedModel playerModel = loader.createTexturedModel("person", "playerTexture", 1, 0);
        Player player = new Player(playerModel, new Vector3f(px, py, pz), 0, 2, 0, 0.6f);
        entities.add(player);

        for(int i=0; i<500; i++){
            Enemy enemy = new Enemy(playerModel, new Vector3f(random.nextFloat() - 2, py, random.nextFloat() - 2), 0, 2, 0, 0.6f);
            entities.add(enemy);
            enemies.add(enemy);
        }

        Camera camera1 = new GameCamera(player);
        camera1.getPosition().set(0, 20, 0);

        Camera camera2 = new PlayerCamera();
        camera2.getPosition().set(0, 30, 0);

        Camera camera = camera1;

        // Water
        WaterFrameBuffers buffers = new WaterFrameBuffers();

        WaterShader waterShader = new WaterShader();
        WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
        List<WaterTile> waters = new ArrayList<>();
        WaterTile water = new GameWaterTile(0, 0, waterHeight, waterSize);
        waters.add(water);
        water = new GameWaterTile(-1 * waterSize, 0, waterHeight, waterSize);
        waters.add(water);
        water = new GameWaterTile(-1 * waterSize, -1 * waterSize, waterHeight, waterSize);
        waters.add(water);
        water = new GameWaterTile(0, -1 * waterSize, waterHeight, waterSize);
        waters.add(water);


        GuiRenderer guiRenderer = new GuiRenderer(loader);

        MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), world);

        //****************Game Loop Below*********************
        int player_hp = 100;
        Update_Health_Gui();
        while (!Display.isCloseRequested()) {
            player.move(world, enemies);
            for(int i=0; i<enemies.size(); i++){
                if(enemies.get(i).getHealth() > 0){enemies.get(i).moving(player.getPosition(), player);}
                else {
                    if(entities.contains(enemies.get(i))){entities.remove(enemies.get(i)); enemies.remove(enemies.get(i));}

                }
            }
            if(!Keyboard.isKeyDown(Keyboard.KEY_T)){
                player.takeDamage(10);
            }

            if(player_hp - player.getHealth() <= 10){
                //if(player.getHealth() > 10){Update_Health_Gui();}
            }
            System.out.println(player_hp);
            System.out.println(player.getHealth());


            camera.move();

            picker.update();
            Vector3f terrainPoint = picker.getCurrentTerrainPoint();
            if (terrainPoint != null) {
                lampEntity.setPosition(terrainPoint);
                lampLight.setPosition(new Vector3f(terrainPoint.x, terrainPoint.y + 14, terrainPoint.z));
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

            guiRenderer.render(guiTextures);

            TextMaster.render();

            // frames = 0 means a new second
            int frames = DisplayManager.updateDisplay();
            player_hp = player.getHealth();
        }

        buffers.cleanUp();
        waterShader.cleanUp();
        TextMaster.cleanUp();
        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }


}
