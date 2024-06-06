#version 400 core

// Input vertex attributes (from the vertex buffer)
in vec3 position; // Vertex position
in vec2 textureCoordinates; // Vertex texture coordinates
in vec3 normal; // Vertex normal vector

// Output variables (passed to the fragment shader)
out vec3 color; // Vertex color
out vec2 pass_textureCoordinates; // Texture coordinates
out vec3 surfaceNormal; // Transformed normal vector
out vec3 toLightVector[4]; // Vectors to light sources
out vec3 toCameraVector; // Vector to the camera
out float visibility; // Visibility factor for fog

// Uniform variables (constants for all vertices)
uniform mat4 transformationMatrix; // Transform matrix
uniform mat4 projectionMatrix; // Projection matrix
uniform mat4 viewMatrix; // View matrix
uniform vec3 lightPosition[4]; // Light source positions

uniform float useFakeLighting; // Fake lighting flag
uniform float skyDensity; // Fog density
uniform float skyGradient; // Fog gradient
uniform float numberOfRows; // Texture atlas rows
uniform vec2 textureOffset; // Texture atlas offset

uniform vec4 clipPlane; // Clipping plane

void main(void)
{
    // Transform to world space (converted to local coordinates)
    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);

    // Compute clipping distance
    gl_ClipDistance[0] = dot(worldPosition, clipPlane);

    // Transform to camera and screen space
    vec4 positionRelativeToCam = viewMatrix * worldPosition;
    gl_Position = projectionMatrix * positionRelativeToCam;

    // Adjust texture coordinates for atlas
    pass_textureCoordinates = (textureCoordinates / numberOfRows) + textureOffset;

    // Use fake lighting if enabled
    vec3 actualNormal = normal;
    if (useFakeLighting > 0.5) {
        actualNormal = vec3(0.0, 1.0, 0.0);
    }

    // Transform normal to world space
    surfaceNormal = (transformationMatrix * vec4(actualNormal, 0.0)).xyz;

    // Compute vectors to light sources
    for (int i = 0; i < 4; i++) {
        toLightVector[i] = lightPosition[i] - worldPosition.xyz;
    }

    // Compute vector to the camera
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;

    // Compute fog visibility factor
    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * skyDensity), skyGradient));
    visibility = clamp(visibility, 0.0, 1.0);

    // Set vertex color (for debugging)
    color = vec3(position.x, position.y, position.z);
}
