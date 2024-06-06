#version 400 core

// Input variables from the vertex shader
in vec3 color; // Color from the vertex shader
in vec2 pass_textureCoordinates; // Texture coordinates from the vertex shader
in vec3 surfaceNormal; // Surface normal from the vertex shader
in vec3 toLightVector[4]; // Vectors to the light sources
in vec3 toCameraVector; // Vector to the camera
in float visibility; // Visibility factor for fog

// Output variable
out vec4 out_Color; // Final color output

// Uniform variables
uniform sampler2D modelTexture; // Texture sampler
uniform vec3 lightColor[4]; // Colors of the light sources
uniform vec3 attenuation[4]; // Attenuation factors for the lights
uniform float shineDamper; // Shine damper for specular highlights
uniform float reflectivity; // Reflectivity factor for specular highlights
uniform vec3 skyColor; // Color of the sky
uniform float shadingLevels; // Levels of shading for cel shading effect

void main(void)
{
    // Normalize input vectors
    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitVectorToCamera = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    // Loop through all light sources
    for (int i = 0; i < 4; i++) {
        // Calculate the distance from the vertex to the current light source
        float distance = length(toLightVector[i]);

        // Calculate the attenuation factor based on distance
        // Attenuation is used to reduce the intensity of light as it moves further from the source
        float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);

        // Normalize the vector to the light source
        vec3 unitLightVector = normalize(toLightVector[i]);

        // Calculate the dot product between the normal vector and the light vector
        // This gives the cosine of the angle between them, which is used to determine brightness
        float nDot1 = dot(unitNormal, unitLightVector);

        // Ensure brightness is at least 0 (no negative values)
        // This means if the light is behind the surface, it will not contribute to brightness
        float brightness = max(nDot1, 0.0);


        // Apply cel shading if shading levels are specified
        if (shadingLevels > 0.1) {
            float level = floor(brightness * shadingLevels);
            brightness = level / shadingLevels;
        }

        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
        specularFactor = max(specularFactor, 0.0);
        float dampedFactor = pow(specularFactor, shineDamper);

        // Apply cel shading to specular highlights
        if (shadingLevels > 0.1) {
            float level = floor(dampedFactor * shadingLevels);
            dampedFactor = level / shadingLevels;
        }

        totalDiffuse += (brightness * lightColor[i]) / attFactor;
        totalSpecular += (dampedFactor * reflectivity * lightColor[i]) / attFactor;
    }

    // Ensure minimum ambient brightness
    float ambientBrightness = 0.2;
    totalDiffuse = max(totalDiffuse, ambientBrightness);

    // Sample the texture
    vec4 textureColor = texture(modelTexture, pass_textureCoordinates);
    if (textureColor.a < 0.5) {
        discard; // Discard fragments with low alpha
    }

    // Calculate final color
    out_Color = vec4(totalDiffuse, 1.0) * textureColor + vec4(totalSpecular, 1.0);
    out_Color = mix(vec4(skyColor, 1), out_Color, visibility); // Apply fog effect
}
