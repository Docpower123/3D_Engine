package org.example.Engine.terrains;

import org.example.Engine.water.WaterTile;
import org.joml.Vector3f;

import java.util.List;

public interface World {
    public float getHeightOfTerrain(float worldX, float worldZ);
    public Vector3f getTerrainPoint(float worldX, float worldZ, float yOffset);
    public float getHeightOfWater(float worldX, float worldZ);
    public List<Terrain> getTerrains();
    public List<WaterTile> getWaterTiles();
    public Terrain getTerrain(float worldX, float worldZ);
    public float getTerrainSize();
    public float getXSize();
    public float getZSize();
}
