#version 400 core

// Input vertex attributes (from the vertex buffer)
in vec2 position; // The position of the vertex

// Output variables (passed to the fragment shader)
out vec4 clipSpace; // Clip space coordinates
out vec2 textureCoords; // Texture coordinates
out vec3 toCameraVector; // Vector to the camera
out vec3 fromLightVector[4]; // Vectors from the light sources

// Uniform variables (constants for all vertices)
uniform mat4 projectionMatrix; // Matrix to project the vertex to screen space
uniform mat4 viewMatrix; // Matrix to transform the vertex to camera space
uniform mat4 modelMatrix; // Matrix to transform the vertex to world space
uniform vec3 cameraPosition; // Position of the camera

uniform float tiling; // Tiling factor for texture coordinates

uniform vec3 lightPosition[4]; // Positions of the light sources

out float visibility; // Visibility factor for fog

uniform float skyDensity; // Density of the fog
uniform float skyGradient; // Gradient of the fog

void main(void)
{
    // Transform to world space
    vec4 worldPosition = modelMatrix * vec4(position.x, 0.0, position.y, 1.0);

    // Transform to camera space
    vec4 positionRelativeToCam = viewMatrix * worldPosition;

    // Transform to clip space
    clipSpace = projectionMatrix * positionRelativeToCam;
    gl_Position = clipSpace;

    // Calculate texture coordinates with tiling
    textureCoords = vec2(position.x / 2.0 + 0.5, position.y / 2.0 + 0.5) * tiling;

    // Calculate vector to the camera
    toCameraVector = cameraPosition - worldPosition.xyz;

    // Calculate vectors from the light sources
    for (int i = 0; i < 4; i++) {
        fromLightVector[i] = worldPosition.xyz - lightPosition[i];
    }

    // Calculate fog visibility factor
    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * skyDensity), skyGradient));
    visibility = clamp(visibility, 0.0, 1.0);
}
