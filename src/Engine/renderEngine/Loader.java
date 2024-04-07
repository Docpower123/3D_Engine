package com.example.Engine.renderEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.example.Engine.normalMappingObjConverter.NormalMappedObjLoader;
import com.example.Engine.textures.ModelTexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;

import com.example.Engine.textures.Texture;
import com.example.Engine.textures.TextureLoader;

import com.example.Engine.models.RawModel;
import com.example.Engine.models.TexturedModel;
import com.example.Engine.objConverter.OBJFileLoader;
import com.example.Engine.textures.TextureData;

public class Loader {

    private final static float LOD_BIAS = -0.4f;

    private List<Integer> vaos = new ArrayList<>();
    private List<Integer> vbos = new ArrayList<>();
    private List<Integer> textures = new ArrayList<>();
    public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }
    public int loadToVAO(float[] positions, float[] textureCoords) {
        int vaoID = createVAO();
        storeDataInAttributeList(0, 2, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        unbindVAO();
        return vaoID;
    }
    public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, float[] tangents, int[] indices) {
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        storeDataInAttributeList(3, 3, tangents);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }
    public RawModel loadToVAO(float[] positions, int dimensions) {
        int vaoID = createVAO();
        storeDataInAttributeList(0, dimensions, positions);
        unbindVAO();
        return new RawModel(vaoID, positions.length / dimensions);
    }
    public int loadTexture(String fileName, float lodBias) {
        Texture texture = null;
        fileName = "res/" + fileName + ".png";

        try {
            texture = TextureLoader.getTexture("PNG", new FileInputStream(fileName));
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, lodBias);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Loader: File not found: " + fileName);
            System.exit(-1);
        }
        int textureID = texture.getTextureID();
        textures.add(textureID);
        return textureID;
    }

    public int loadTexture(String fileName) {
        return loadTexture(fileName, LOD_BIAS);
    }

    public int loadFontTextureAtlas(String fileName) {
        return loadTexture("fonts/" + fileName, 0);
    }
    
    public void cleanUp() {
        for (int vao : vaos) {
            GL30.glDeleteVertexArrays(vao);
        }
        for (int vbo : vbos) {
            GL15.glDeleteBuffers(vbo);
        }
        for (int texture : textures) {
            GL11.glDeleteTextures(texture);
        }
    }

    public int loadCubeMap(String[] textureFiles) {
        int texID = GL11.glGenTextures();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texID);

        for (int i = 0; i < textureFiles.length; i++) {
            TextureData data = decodeTextureFile("res/" + textureFiles[i] + ".png");
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA,
                    data.getWidth(), data.getHeight(),0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                    data.getBuffer());
            data.freeBuffer();
        }
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        textures.add(texID);
        return texID;
    }

    private ByteBuffer readByteBufferFromFileInputStream(FileInputStream fs) throws IOException {
        FileChannel fc = fs.getChannel();
        ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        fc.close();
        fs.close();
        return buffer;
    }
    private TextureData decodeTextureFile(String fileName) {
        int width = 0;
        int height = 0;
        ByteBuffer buffer = null;

        try {
            FileInputStream fs = new FileInputStream(fileName);
            IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer componentsBuffer = BufferUtils.createIntBuffer(1);
            ByteBuffer byteBuffer = readByteBufferFromFileInputStream(fs);
            ByteBuffer data = STBImage.stbi_load_from_memory(byteBuffer, widthBuffer, heightBuffer, componentsBuffer, 4);
            width = widthBuffer.get(0);
            height = heightBuffer.get(0);
            buffer = data;
            fs.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Loader: File not found: " + fileName);
            System.exit(-1);
        }
        return new TextureData(buffer, width, height);
    }

    private int createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }
    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    private void bindIndicesBuffer(int[] indices) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }
    public TexturedModel createTexturedModel(
            String objFileName,
            String textureFileName,
            float materialShineDamper,
            float materialReflectivity) {
        RawModel model = OBJFileLoader.loadOBJ(objFileName, this);
        ModelTexture texture = new ModelTexture(this.loadTexture(textureFileName));
        texture.setShineDamper(materialShineDamper);
        texture.setReflectivity(materialReflectivity);
        TexturedModel texturedModel = new TexturedModel(model, texture);
        return texturedModel;
    }
    
    public TexturedModel createTexturedModel(
            String objFileName,
            String textureFileName,
            float materialShineDamper,
            float materialReflectivity,
            boolean hasTransparency,
            boolean useFakeLighting
            ) {
        TexturedModel texturedModel = createTexturedModel(
                objFileName, textureFileName, materialShineDamper, materialReflectivity);
        texturedModel.getTexture().setHasTransparency(hasTransparency);
        texturedModel.getTexture().setUseFakeLighting(useFakeLighting);
        return texturedModel;
    }
    public TexturedModel createTexturedModel(
            String objFileName,
            String textureFileName,
            int numberOfRows,
            float materialShineDamper,
            float materialReflectivity,
            boolean hasTransparency,
            boolean useFakeLighting
            ) {
        TexturedModel texturedModel = createTexturedModel(
                objFileName, textureFileName,
                materialShineDamper, materialReflectivity,
                hasTransparency, useFakeLighting);
        texturedModel.getTexture().setNumberOfRows(numberOfRows);
        return texturedModel;
    }

    public TexturedModel createTexturedModel(
            String objFileName,
            String textureFileName,
            String normalMapFileName,
            float materialShineDamper,
            float materialReflectivity) {
        RawModel model = NormalMappedObjLoader.loadOBJ(objFileName, this);
        ModelTexture texture = new ModelTexture(this.loadTexture(textureFileName));
        int normalMap = loadTexture(normalMapFileName);
        texture.setNormalMap(normalMap);
        texture.setShineDamper(materialShineDamper);
        texture.setReflectivity(materialReflectivity);
        TexturedModel texturedModel = new TexturedModel(model, texture);
        return texturedModel;
    }
}
