package org.example.Engine.terrains;

import org.example.Engine.Loader;
import org.example.Engine.models.RawModel;
import org.example.Engine.textures.ModelTexture;
import org.example.Engine.textures.TerrainTexture;
import org.example.Engine.textures.TerrainTexturePack;
import org.example.Engine.toolbox.Maths;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TerrainWater implements Terrain {

    private static final float MAX_PIXEL_COLOR = 256 * 256 * 256;
    private static final float HEIGHT_OFFSET = 0;

    private float x;
    private float z;
    private float size;
    private float maxHeight;
    private RawModel model;
    private TerrainTexturePack texturePack;
    private TerrainTexture blendMap;

    private float[][] heights;

    /**
     * Constructs a water terrain tile.
     *
     * @param gridX          The x position of the terrain grid.
     * @param gridZ          The z position of the terrain grid.
     * @param size           The size of the terrain tile.
     * @param maxHeight      The maximum height of the terrain.
     * @param loader         The loader used to load the terrain model.
     * @param texturePack    The texture pack for the terrain.
     * @param blendMap       The blend map for the terrain textures.
     * @param heightMap      The height map image file for the terrain.
     */
    public TerrainWater(int gridX, int gridZ, float size, float maxHeight, Loader loader, TerrainTexturePack texturePack,
                        TerrainTexture blendMap, String heightMap) {
        this.texturePack = texturePack;
        this.blendMap = blendMap;
        this.size = size;
        this.maxHeight = maxHeight;
        this.x = gridX * size;
        this.z = gridZ * size;
        this.model = generateTerrain(loader, heightMap);
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public float getSize() {
        return size;
    }

    public Vector3f getPosition() {
        return new Vector3f(x, 0, z);
    }

    public RawModel getModel() {
        return model;
    }

    public ModelTexture getTexture() {
        return null;
    }

    public TerrainTexturePack getTexturePack() {
        return texturePack;
    }

    public TerrainTexture getBlendMap() {
        return blendMap;
    }

    /**
     * Checks if the specified world coordinates are within this terrain tile.
     *
     * @param worldX The x coordinate in the world.
     * @param worldZ The z coordinate in the world.
     * @return True if the coordinates are within this terrain tile, false otherwise.
     */
    public boolean containsPosition(float worldX, float worldZ) {
        return !(worldX < x || worldX >= x + size || worldZ < z || worldZ >= z + size);
    }

    public float getHeightOfWater() {
        return 0;
    }

    /**
     * Gets the height of the terrain at the specified world coordinates.
     *
     * @param worldX The x coordinate in the world.
     * @param worldZ The z coordinate in the world.
     * @return The height of the terrain at the specified coordinates.
     */
    public float getHeightOfTerrain(float worldX, float worldZ) {
        float terrainX = worldX - this.x;
        float terrainZ = worldZ - this.z;
        float gridSquareSize = size / ((float) heights.length - 1);
        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
        if (gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0) {
            return 0;
        }
        float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
        float answer;

        if (xCoord <= (1 - zCoord)) {
            answer = Maths.baryCentric(
                    new Vector3f(0, heights[gridX][gridZ], 0),
                    new Vector3f(1, heights[gridX + 1][gridZ], 0),
                    new Vector3f(0, heights[gridX][gridZ + 1], 1),
                    new Vector2f(xCoord, zCoord));
        } else {
            answer = Maths.baryCentric(
                    new Vector3f(1, heights[gridX + 1][gridZ], 0),
                    new Vector3f(1, heights[gridX + 1][gridZ + 1], 1),
                    new Vector3f(0, heights[gridX][gridZ + 1], 1),
                    new Vector2f(xCoord, zCoord));
        }
        return answer;
    }

    /**
     * Generates the terrain model based on the height map.
     *
     * @param loader The loader used to load the terrain model.
     * @param heightMap The height map image file.
     * @return The generated RawModel.
     */
    private RawModel generateTerrain(Loader loader, String heightMap) {
        BufferedImage image = null;
        String fileName = "res/" + heightMap + ".png";
        try {
            image = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            System.err.println("Terrain: File not found: " + fileName);
            e.printStackTrace();
        }
        int VERTEX_COUNT = image.getHeight();

        int count = VERTEX_COUNT * VERTEX_COUNT;
        heights = new float[VERTEX_COUNT][VERTEX_COUNT];
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];

        int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];

        int vertexPointer = 0;
        for (int i = 0; i < VERTEX_COUNT; i++) {
            for (int j = 0; j < VERTEX_COUNT; j++) {
                vertices[vertexPointer * 3] = (float) j / ((float) VERTEX_COUNT - 1) * size;
                float height = getHeight(j, i, image);
                vertices[vertexPointer * 3 + 1] = height;
                heights[j][i] = height;
                vertices[vertexPointer * 3 + 2] = (float) i / ((float) VERTEX_COUNT - 1) * size;
                Vector3f normal = calculateNormal(j, i, image);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;
                textureCoords[vertexPointer * 2] = (float) j / ((float) VERTEX_COUNT - 1);
                textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) VERTEX_COUNT - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
            for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
                int topLeft = (gz * VERTEX_COUNT) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return loader.loadToVAO(vertices, textureCoords, normals, indices);
    }

    /**
     * Calculates the normal vector at the specified coordinates in the height map.
     *
     * @param x     The x coordinate.
     * @param z     The z coordinate.
     * @param image The height map image.
     * @return The calculated normal vector.
     */
    private Vector3f calculateNormal(int x, int z, BufferedImage image) {
        float heightL = getHeight(x - 1, z, image);
        float heightR = getHeight(x + 1, z, image);
        float heightD = getHeight(x, z - 1, image);
        float heightU = getHeight(x, z + 1, image);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalize();
        return normal;
    }

    /**
     * Gets the height at the specified coordinates in the height map.
     *
     * @param x     The x coordinate.
     * @param z     The z coordinate.
     * @param image The height map image.
     * @return The height at the specified coordinates.
     */
    private float getHeight(int x, int z, BufferedImage image) {
        if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) {
            return HEIGHT_OFFSET;
        }
        float height = image.getRGB(x, z);
        height += MAX_PIXEL_COLOR / 2f;
        height /= MAX_PIXEL_COLOR / 2f;
        height *= maxHeight;
        height += HEIGHT_OFFSET;
        return height;
    }
}
