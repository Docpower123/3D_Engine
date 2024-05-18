package org.example.Engine.terrains;

import org.example.Engine.Loader;
import org.example.Engine.textures.TerrainTexture;
import org.example.Engine.textures.TerrainTexturePack;
import org.example.Engine.water.WaterTile;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class GameWorld implements World {

    float waterHeight;
    List<Terrain> terrains;

    /**
     * Constructs a GameWorld with the specified terrain parameters.
     *
     * @param loader            The loader used to load textures and models.
     * @param terrainSize       The size of each terrain tile.
     * @param terrainMaxHeight  The maximum height of the terrain.
     * @param waterHeight       The height of the water in the world.
     */
    public GameWorld(Loader loader, float terrainSize, float terrainMaxHeight, float waterHeight) {

        // *********TERRAIN TEXTURE STUFF**********

        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("rockDiffuse"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("mossPath256"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture,
                rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

        terrains = new ArrayList<>();

        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                Terrain terrain = new TerrainWater(x, z, terrainSize, terrainMaxHeight, loader, texturePack, blendMap, "heightmap");
                terrains.add(terrain);
            }
        }

        System.out.println("World: generated " + terrains.size() + " terrains.");

        this.waterHeight = waterHeight;
    }

    /**
     * Gets the height of the terrain at the specified world coordinates.
     *
     * @param worldX The x coordinate in the world.
     * @param worldZ The z coordinate in the world.
     * @return The height of the terrain at the specified coordinates.
     */
    public float getHeightOfTerrain(float worldX, float worldZ) {
        float height = 0;
        Terrain terrain = getTerrain(worldX, worldZ);

        // if we got a terrain, get terrain height
        if (terrain != null) {
            height = terrain.getHeightOfTerrain(worldX, worldZ);
        }

        return height;
    }

    /**
     * Returns a point in space that is at the specified world coordinates, offset by yOffset units above the terrain.
     *
     * @param worldX  The x coordinate in the world.
     * @param worldZ  The z coordinate in the world.
     * @param yOffset The y offset above the terrain.
     * @return The point in space at the specified coordinates.
     */
    public Vector3f getTerrainPoint(float worldX, float worldZ, float yOffset) {
        float y = getHeightOfTerrain(worldX, worldZ) + yOffset;
        return new Vector3f(worldX, y, worldZ);
    }

    /**
     * Gets the height of the water at the specified world coordinates.
     *
     * @param worldX The x coordinate in the world.
     * @param worldZ The z coordinate in the world.
     * @return The height of the water at the specified coordinates.
     */
    public float getHeightOfWater(float worldX, float worldZ) {
        return waterHeight;
    }

    /**
     * Gets the list of terrains in the world.
     *
     * @return The list of terrains.
     */
    public List<Terrain> getTerrains() {
        return terrains;
    }

    /**
     * Gets the terrain at the specified world coordinates.
     *
     * @param worldX The x coordinate in the world.
     * @param worldZ The z coordinate in the world.
     * @return The terrain at the specified coordinates, or null if no terrain is found.
     */
    public Terrain getTerrain(float worldX, float worldZ) {
        for (Terrain terrain : terrains) {
            if (terrain.containsPosition(worldX, worldZ)) {
                return terrain;
            }
        }
        return null;
    }

    /**
     * Gets the list of water tiles in the world.
     *
     * @return The list of water tiles, or null if none are found.
     */
    public List<WaterTile> getWaterTiles() {
        return null;
    }

    // Not used methods
    /**
     * Gets the size of the terrain.
     *
     * @return The size of the terrain.
     */
    public float getTerrainSize() {
        return 0;
    }

    /**
     * Gets the x size of the terrain.
     *
     * @return The x size of the terrain.
     */
    public float getXSize() {
        return 0;
    }

    /**
     * Gets the z size of the terrain.
     *
     * @return The z size of the terrain.
     */
    public float getZSize() {
        return 0;
    }
}
