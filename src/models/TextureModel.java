package models;

import renderEngine.RawModel;

public class TextureModel {

    private RawModel rawModel;
    private int textureID;

    public TextureModel(RawModel rawModel, int textureID) {
        this.rawModel = rawModel;
        this.textureID = textureID;
    }

    public TextureModel(int textureID) {
    }

    public RawModel getRawModel() {
        return rawModel;
    }

    public int getTextureID() {
        return textureID;
    }
}
