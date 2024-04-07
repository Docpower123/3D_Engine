package com.example.Engine.terrains;

import java.util.List;

import com.example.Engine.water.WaterTile;
import org.joml.Vector3f;

public interface World {
    public float getHeightOfTerrain(float worldX, float worldZ);
    public Vector3f getTerrainPoint(float worldX, float worldZ, float yOffset);
    public float getHeightOfWater(float worldX, float worldZ);
    public List<Terrain> getTerrains();
    public List<WaterTile> getWaterTiles();
    public Terrain getTerrain(float worldX, float worldZ);

    // size of each terrain tile
    public float getTerrainSize();
    public float getXSize();
    public float getZSize();
}
