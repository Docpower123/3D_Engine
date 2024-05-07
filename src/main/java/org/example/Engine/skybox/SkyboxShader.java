package org.example.Engine.skybox;


import org.example.Engine.Display_Manager;
import org.example.Engine.entities.Camera;
import org.example.Engine.shaders.ShaderProgram;
import org.example.Engine.toolbox.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SkyboxShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/main/java/org/example/Engine/skybox/skyboxVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src/main/java/org/example/Engine/skybox/skyboxFragmentShader.glsl";

    private static final float ROTATE_SPEED = 0.1f; // was 1f

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_skyColor;
    private int location_cubeMap;
    private int location_cubeMap2;
    private int location_blendFactor;
    private int location_lowerLimit;
    private int location_upperLimit;
    private int location_shadingLevels;

    private float rotation = 0;

    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        // remove translation from view matrix
        matrix.m30(0);
        matrix.m31(0);
        matrix.m32(0);
        rotation += ROTATE_SPEED * Display_Manager.getFrameTimeSeconds();
        super.loadMatrix(location_viewMatrix, matrix);
    }

    public void loadSkyColor(float r, float g, float b) {
        super.loadVector(location_skyColor, new Vector3f(r, g, b));
    }

    public void connectTextureUnits() {
        super.loadInt(location_cubeMap, 0);
        super.loadInt(location_cubeMap2, 1);
    }

    public void loadBlendFactor(float blend) {
        super.loadFloat(location_blendFactor, blend);
    }
    
    public void loadLimits(float lowerLimit, float upperLimit) {
        super.loadFloat(location_lowerLimit, lowerLimit);
        super.loadFloat(location_upperLimit, upperLimit);
    }
    
    public void loadShadingLevels(float levels) {
        super.loadFloat(location_shadingLevels, levels);
    }

    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_skyColor = super.getUniformLocation("skyColor");
        location_blendFactor = super.getUniformLocation("blendFactor");
        location_cubeMap = super.getUniformLocation("cubeMap");
        location_cubeMap2 = super.getUniformLocation("cubeMap2");
        location_lowerLimit = super.getUniformLocation("lowerLimit");
        location_upperLimit = super.getUniformLocation("upperLimit");
        location_shadingLevels = super.getUniformLocation("shadingLevels");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

}