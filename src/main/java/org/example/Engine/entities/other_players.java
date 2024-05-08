package org.example.Engine.entities;

import org.example.Engine.models.TexturedModel;
import org.joml.Vector3f;

public class other_players extends Entity{

    public other_players(TexturedModel model, Vector3f position, float scale) {
        super(model, position, scale);
    }

    //TODO: implement jumping
    public void moving(Vector3f position) {
        super.setPosition(position);
    }

}
