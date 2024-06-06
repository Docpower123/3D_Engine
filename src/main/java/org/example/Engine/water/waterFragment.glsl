#version 400 core

// Input variables (from the vertex shader)
in vec4 clipSpace; // Clip space coordinates
in vec2 textureCoords; // Texture coordinates
in vec3 toCameraVector; // Vector to the camera
in vec3 fromLightVector[4]; // Vectors from the light sources

in float visibility; // Visibility factor for fog

// Output variable
out vec4 out_Color; // Final color output

// Uniform variables (constants for all fragments)
uniform vec3 skyColor; // Color of the sky

uniform sampler2D reflectionTexture; // Reflection texture sampler
uniform sampler2D refractionTexture; // Refraction texture sampler
uniform sampler2D dudvMap; // DuDv map sampler
uniform float moveFactor; // Movement factor for DuDv map animation
uniform float waveStrength; // Strength of the water waves

const float waterReflectivity = 2.0; // Reflectivity factor for water

uniform sampler2D normalMap; // Normal map sampler

uniform vec3 lightColor[4]; // Colors of the light sources
uniform vec3 attenuation[4]; // Attenuation factors for the lights

const float shineDamper = 200.0; // Shine damper for specular highlights
const float reflectivity = 10; // Reflectivity factor for specular highlights

uniform sampler2D depthMap; // Depth map sampler

uniform float shadingLevels; // Levels of shading for cel shading effect

void main(void) {

    // Normalize device coordinates
    vec2 ndc = (clipSpace.xy / clipSpace.w) / 2.0 + 0.5;
    vec2 refractTexCoords = vec2(ndc.x, ndc.y);
    vec2 reflectTexCoords = vec2(ndc.x, -ndc.y);

    float near = 0.1;
    float far = 100000.0;
    // Depth info in r channel
    float depth = texture(depthMap, refractTexCoords).r;
    float floorDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));

    depth = gl_FragCoord.z;
    float waterDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    float waterDepth = floorDistance - waterDistance;

    // Apply DuDv map distortion
    vec2 distortedTexCoords = texture(dudvMap, vec2(textureCoords.x + moveFactor, textureCoords.y)).rg * 0.1;
    distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y + moveFactor);
    vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength;
    totalDistortion *= clamp(waterDepth / 20, 0.0, 1.0);

    // Adjust texture coordinates with distortion
    refractTexCoords += totalDistortion;
    refractTexCoords = clamp(refractTexCoords, 0.001, 0.999);
    reflectTexCoords += totalDistortion;
    reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001, 0.999);
    reflectTexCoords.y = clamp(reflectTexCoords.y, -0.999, -0.001);

    // Sample reflection and refraction textures
    vec4 reflectColor = texture(reflectionTexture, reflectTexCoords);
    vec4 refractColor = texture(refractionTexture, refractTexCoords);

    // Sample normal map
    vec4 normalMapColor = texture(normalMap, distortedTexCoords);
    vec3 normal = vec3(normalMapColor.r * 2.0 - 1.0, normalMapColor.b * 3, normalMapColor.g * 2.0 - 1.0);
    normal = normalize(normal);

    vec3 viewVector = normalize(toCameraVector);
    float refractiveFactor = dot(viewVector, normal);
    // Adjust reflectivity based on view angle
    refractiveFactor = pow(refractiveFactor, waterReflectivity);

    vec3 totalSpecularHighlights = vec3(0.0);

    // Calculate specular highlights for each light source
    for (int i = 0; i < 4; i++) {
        float distance = length(fromLightVector[i]);
        float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);

        vec3 reflectedLight = reflect(normalize(fromLightVector[i]), normal);
        float specular = max(dot(reflectedLight, viewVector), 0.0);
        specular = pow(specular, shineDamper);

        // Apply cel shading if shading levels are specified
        float level = 0;
        if (shadingLevels > 0.1) {
            level = floor(specular * shadingLevels);
            specular = level / shadingLevels;
        }

        totalSpecularHighlights += lightColor[i] * specular * reflectivity / attFactor;
    }

    // Mix reflection and refraction colors based on refractive factor
    out_Color = mix(reflectColor, refractColor, refractiveFactor);

    // Add base water color
    out_Color = mix(out_Color, vec4(0.0, 0.3, 0.5, 1.0), 0.2);

    totalSpecularHighlights *= clamp(waterDepth / 5, 0.0, 1.0);

    // Add specular highlights
    out_Color += vec4(totalSpecularHighlights, 0.0);

    // Adjust alpha based on water depth
    out_Color.a = clamp(waterDepth / 50, 0.0, 1.0);

    // Apply fog based on visibility factor
    out_Color = mix(vec4(skyColor, 1), out_Color, visibility);
}
