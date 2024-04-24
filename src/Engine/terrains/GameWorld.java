package com.example.Engine.terrains;

import java.util.ArrayList;
import java.util.List;

import com.example.Engine.water.WaterTile;
import org.joml.Vector3f;

import com.example.Engine.renderEngine.Loader;
import com.example.Engine.textures.TerrainTexture;
import com.example.Engine.textures.TerrainTexturePack;

public class GameWorld implements World {

    float waterHeight;
    
    List<Terrain> terrains;
    
    public GameWorld(Loader loader, float terrainSize, float terrainMaxHeight, float waterHeight) {
        
        // *********TERRAIN TEXTURE STUFF**********

        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("rockDiffuse"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("mossPath256"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture,
                rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
        
        terrains = new ArrayList<Terrain>();
        
        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                Terrain terrain = new TerrainWater(x, z, terrainSize, terrainMaxHeight, loader, texturePack, blendMap, "heightmap");
                terrains.add(terrain);
            }
        }
        
        System.out.println(STR."World: generated \{terrains.size()} terrains.");
        
        this.waterHeight = waterHeight;
    }
    
    public float getHeightOfTerrain(float worldX, float worldZ) {
        float height = 0;
        Terrain terrain = getTerrain(worldX, worldZ);
    
        // if we got a terrain, get terrain height
        if (terrain != null) {
            height = terrain.getHeightOfTerrain(worldX, worldZ);
        }

        return height;
    }
    
    // return a point in space that is at worldX, worldZ, at yOffset units above the terrain
    public Vector3f getTerrainPoint(float worldX, float worldZ, float yOffset) {
        float y = getHeightOfTerrain(worldX, worldZ) + yOffset;
        return new Vector3f(worldX, y, worldZ);
    }
    
    public float getHeightOfWater(float worldX, float worldZ) {
        return waterHeight;
    }
    
    public List<Terrain> getTerrains() {
        return terrains;
    }
    
    public Terrain getTerrain(float worldX, float worldZ) {
        for (int i = 0; i < terrains.size(); i++) {
            Terrain terrain = terrains.get(i);
            if (terrain.containsPosition(worldX, worldZ)) {
                return terrain;
            }
        }
        return null;
    }
    public List<WaterTile> getWaterTiles() {
        return null;
    }
    
    // not used
    public float getTerrainSize() {
        return 0;
    }
    
    public float getXSize() {
        return 0;
    }
    
    public float getZSize() {
        return 0;
    }
}
