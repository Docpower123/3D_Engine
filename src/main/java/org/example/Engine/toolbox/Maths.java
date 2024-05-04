package org.example.Engine.toolbox;

import org.example.Engine.entities.Camera;
import org.example.Engine.Display_Manager;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Maths {

    public static float baryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Matrix4f createTransformationMatrix(
            Vector2f translation, Vector2f scale) {


        Matrix4f m = new Matrix4f();

        m.translate(translation.x, translation.y, 0);
        m.scale(scale.x, scale.y, 1.0f);
        m.transpose();
        
        return m;
    }

    public static Matrix4f createTransformationMatrix(
            Vector3f translation,
            float rx,
            float ry,
            float rz,
            float scale) {

        Matrix4f m = new Matrix4f();

        m.translate(translation);
        m.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0));
        m.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0));
        m.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1));
        m.scale(scale);

        m.transpose();

        return m;
    }

    public static Matrix4f createProjectionMatrix(float fov, float nearPlane, float farPlane) {
        float aspectRatio = (int) Display_Manager.getWidth() / (int) Display_Manager.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(fov / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustumLength = farPlane - nearPlane;

        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.m00(x_scale);
        projectionMatrix.m11(y_scale);
        projectionMatrix.m22(-((farPlane + nearPlane) / frustumLength));
        projectionMatrix.m23(-1);
        projectionMatrix.m32(-((2 * nearPlane * farPlane) / frustumLength));
        projectionMatrix.m33(0);

        projectionMatrix.transpose();

        return projectionMatrix;
    }

    public static Matrix4f createViewMatrix(Camera camera) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0));
        viewMatrix.rotate((float) Math.toRadians(camera.getYaw()),   new Vector3f(0, 1, 0));
        viewMatrix.rotate((float) Math.toRadians(camera.getRoll()),  new Vector3f(0, 0, 1));
        Vector3f cameraPos = camera.getPosition();
        Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        viewMatrix.translate(negativeCameraPos);

        viewMatrix.transpose();

        return viewMatrix;
    }

    public static Matrix4f createViewMatrix(
            Vector3f cameraPos,
            float pitch,
            float yaw,
            float roll) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0));
        viewMatrix.rotate((float) Math.toRadians(yaw),   new Vector3f(0, 1, 0));
        viewMatrix.rotate((float) Math.toRadians(roll),  new Vector3f(0, 0, 1));
        Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        viewMatrix.translate(negativeCameraPos);

        viewMatrix.transpose();

        return viewMatrix;
    }
}
