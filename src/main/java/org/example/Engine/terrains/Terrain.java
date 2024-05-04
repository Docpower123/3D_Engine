package org.example.Engine.terrains;


import org.example.Engine.models.RawModel;
import org.example.Engine.textures.ModelTexture;
import org.example.Engine.textures.TerrainTexture;
import org.example.Engine.textures.TerrainTexturePack;
import org.joml.Vector3f;

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
