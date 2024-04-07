package com.example.Engine.terrains;

import org.joml.Vector3f;

import com.example.Engine.models.RawModel;
import com.example.Engine.textures.ModelTexture;
import com.example.Engine.textures.TerrainTexture;
import com.example.Engine.textures.TerrainTexturePack;

public interface Terrain {
    public float getX();
    public float getZ();
    public float getSize();
    public Vector3f getPosition();
    public RawModel getModel();
    public ModelTexture getTexture();
    public TerrainTexturePack getTexturePack();
    public TerrainTexture getBlendMap();
    public float getHeightOfTerrain(float worldX, float worldZ);
    public boolean containsPosition(float worldX, float worldZ);
    public float getHeightOfWater();
}
