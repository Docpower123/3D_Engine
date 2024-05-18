package org.example.Engine.water;

import org.example.Engine.entities.Camera;
import org.example.Engine.entities.Light;
import org.example.Engine.shaders.ShaderProgram;
import org.example.Engine.toolbox.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class WaterShader extends ShaderProgram {

    private static final int MAX_LIGHTS = 4;

    private final static String VERTEX_FILE = "src/main/java/org/example/Engine/water/waterVertex.glsl";
    private final static String FRAGMENT_FILE = "src/main/java/org/example/Engine/water/waterFragment.glsl";

    private int location_modelMatrix;
    private int location_viewMatrix;
    private int location_projectionMatrix;
    private int location_reflectionTexture;
    private int location_refractionTexture;
    private int location_dudvMap;
    private int location_waveStrength;
    private int location_tiling;
    private int location_moveFactor;
    private int location_cameraPosition;
    private int location_normalMap;
    private int location_depthMap;

    private int[] location_lightColor;
    private int[] location_lightPosition;
    private int[] location_attenuation;

    private int location_skyColor;
    private int location_skyDensity;
    private int location_skyGradient;

    private int location_shadingLevels;

    /**
     * Constructs the WaterShader and initializes the shader program with vertex and fragment shader files.
     */
    public WaterShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    /**
     * Binds the attributes used in the shader program.
     */
    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
    }

    /**
     * Retrieves all the uniform locations from the shader program.
     */
    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_modelMatrix = getUniformLocation("modelMatrix");
        location_reflectionTexture = getUniformLocation("reflectionTexture");
        location_refractionTexture = getUniformLocation("refractionTexture");
        location_dudvMap = getUniformLocation("dudvMap");
        location_waveStrength = getUniformLocation("waveStrength");
        location_tiling = getUniformLocation("tiling");
        location_moveFactor = getUniformLocation("moveFactor");
        location_cameraPosition = getUniformLocation("cameraPosition");
        location_normalMap = getUniformLocation("normalMap");
        location_depthMap = getUniformLocation("depthMap");

        location_lightPosition = new int[MAX_LIGHTS];
        location_lightColor = new int[MAX_LIGHTS];
        location_attenuation = new int[MAX_LIGHTS];
        for (int i = 0; i < MAX_LIGHTS; i++) {
            location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
            location_lightColor[i] = super.getUniformLocation("lightColor[" + i + "]");
            location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
        }

        location_skyColor = super.getUniformLocation("skyColor");
        location_skyDensity = super.getUniformLocation("skyDensity");
        location_skyGradient = super.getUniformLocation("skyGradient");

        location_shadingLevels = super.getUniformLocation("shadingLevels");
    }

    /**
     * Connects the texture units to the shader program.
     */
    public void connectTextureUnits() {
        super.loadInt(location_reflectionTexture, 0);
        super.loadInt(location_refractionTexture, 1);
        super.loadInt(location_dudvMap, 2);
        super.loadInt(location_normalMap, 3);
        super.loadInt(location_depthMap, 4);
    }

    /**
     * Loads the lights' data into the shader program.
     *
     * @param lights The list of lights to load.
     */
    public void loadLights(List<Light> lights) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                super.loadVector(location_lightPosition[i], lights.get(i).getPosition());
                super.loadVector(location_lightColor[i], lights.get(i).getColor());
                super.loadVector(location_attenuation[i], lights.get(i).getAttenuation());
            } else {
                super.loadVector(location_lightPosition[i], new Vector3f(0, 0, 0));
                super.loadVector(location_lightColor[i], new Vector3f(0, 0, 0));
                super.loadVector(location_attenuation[i], new Vector3f(1, 0, 0));
            }
        }
    }

    /**
     * Loads the move factor for the water waves into the shader program.
     *
     * @param factor The move factor to load.
     */
    public void loadMoveFactor(float factor) {
        super.loadFloat(location_moveFactor, factor);
    }

    /**
     * Loads the tiling factor into the shader program.
     *
     * @param factor The tiling factor to load.
     */
    public void loadTiling(float factor) {
        super.loadFloat(location_tiling, factor);
    }

    /**
     * Loads the wave strength factor into the shader program.
     *
     * @param factor The wave strength factor to load.
     */
    public void loadWaveStrength(float factor) {
        super.loadFloat(location_waveStrength, factor);
    }

    /**
     * Loads the projection matrix into the shader program.
     *
     * @param projection The projection matrix to load.
     */
    public void loadProjectionMatrix(Matrix4f projection) {
        loadMatrix(location_projectionMatrix, projection);
    }

    /**
     * Loads the view matrix and camera position into the shader program.
     *
     * @param camera The camera to use for creating the view matrix.
     */
    public void loadViewMatrix(Camera camera) {
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        loadMatrix(location_viewMatrix, viewMatrix);
        super.loadVector(location_cameraPosition, camera.getPosition());
    }

    /**
     * Loads the model matrix into the shader program.
     *
     * @param modelMatrix The model matrix to load.
     */
    public void loadModelMatrix(Matrix4f modelMatrix) {
        loadMatrix(location_modelMatrix, modelMatrix);
    }

    /**
     * Loads the sky variables into the shader program.
     *
     * @param density The density of the sky.
     * @param gradient The gradient of the sky.
     */
    public void loadSkyVariables(float density, float gradient) {
        super.loadFloat(location_skyDensity, density);
        super.loadFloat(location_skyGradient, gradient);
    }

    /**
     * Loads the sky color into the shader program.
     *
     * @param skyColor The color of the sky.
     */
    public void loadSkyColor(Vector3f skyColor) {
        super.loadVector(location_skyColor, skyColor);
    }

    /**
     * Loads the sky color into the shader program.
     *
     * @param r The red component of the sky color.
     * @param g The green component of the sky color.
     * @param b The blue component of the sky color.
     */
    public void loadSkyColor(float r, float g, float b) {
        super.loadVector(location_skyColor, new Vector3f(r, g, b));
    }

    /**
     * Loads the shading levels into the shader program.
     *
     * @param levels The number of shading levels to load.
     */
    public void loadShadingLevels(float levels) {
        super.loadFloat(location_shadingLevels, levels);
    }
}
