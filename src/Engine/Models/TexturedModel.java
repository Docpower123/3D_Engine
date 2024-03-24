package Engine.models;

import Engine.textures.ModelTexture;

public class TexturedModel {
	
	private RawModel rawModel;
	private ModelTexture texture;
	
	public TexturedModel(RawModel model, ModelTexture texture){
		this.rawModel = model;
		this.texture = texture;
	}

	public Engine.models.RawModel getRawModel() {
		return rawModel;
	}

	public ModelTexture getTexture() {
		return texture;
	}

}
