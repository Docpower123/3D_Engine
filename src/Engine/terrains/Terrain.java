package com.example.Engine.terrains;

import org.joml.Vector3f;

import com.example.Engine.models.RawModel;
import com.example.Engine.textures.ModelTexture;
import com.example.Engine.textures.TerrainTexture;
import com.example.Engine.textures.TerrainTexturePack;

public interface Terrain {
     float getX();
     float getZ();
    float getSize();
    Vector3f getPosition();
    RawModel getModel();
    ModelTexture getTexture();
    public TerrainTexturePack getTexturePack();
    public TerrainTexture getBlendMap();
    public float getHeightOfTerrain(float worldX, float worldZ);
    public boolean containsPosition(float worldX, float worldZ);
    public float getHeightOfWater();
}
