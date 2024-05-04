#version 400 core

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec3 color;
out vec2 pass_textureCoordinates;
out vec3 surfaceNormal;
out vec3 toLightVector[4];
out vec3 toCameraVector;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[4];

uniform float useFakeLighting;
uniform float skyDensity;
uniform float skyGradient;
uniform float numberOfRows;
uniform vec2 textureOffset; 

uniform vec4 clipPlane;

void main(void)
{
    vec4 worldPosition = transformationMatrix * vec4(position.xyz, 1.0);
    
    gl_ClipDistance[0] = dot(worldPosition, clipPlane);
    
    vec4 positionRelativeToCam = viewMatrix * worldPosition;
    gl_Position = projectionMatrix * positionRelativeToCam;
    
    pass_textureCoordinates = (textureCoordinates/numberOfRows) + textureOffset;
    
    vec3 actualNormal = normal;
    if (useFakeLighting > 0.5) {
        actualNormal = vec3(0.0, 1.0, 0.0);
    }
    
    surfaceNormal = (transformationMatrix * vec4(actualNormal, 0.0)).xyz;
    for (int i = 0; i < 4; i++) {
        toLightVector[i] = lightPosition[i] - worldPosition.xyz;
    }
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;

    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * skyDensity), skyGradient));
    visibility = clamp(visibility, 0.0, 1.0);

    color = vec3(position.x, position.y, position.z);
}
