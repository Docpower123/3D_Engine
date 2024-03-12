package engine.objects;

import engine.graphics.Mesh;
import engine.maths.Vector3f;

public class GameObject {
	private Vector3f position, rotation, scale;
	private Mesh mesh;
	private Vector3f color; // New color attribute

	public GameObject(Vector3f position, Vector3f rotation, Vector3f scale, Mesh mesh, Vector3f color) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.mesh = mesh;
		this.color = color;
	}

	public void update() {
		position.setZ(position.getZ() - 0.05f);
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public Vector3f getScale() {
		return scale;
	}

	public Mesh getMesh() {
		return mesh;
	}

	public Vector3f getColor() {
		return color;
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}
}
