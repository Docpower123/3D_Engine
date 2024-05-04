#version 400 core

in vec4 clipSpace;
in vec2 textureCoords;
in vec3 toCameraVector;
in vec3 fromLightVector[4];

in float visibility;

out vec4 out_Color;

uniform vec3 skyColor;

uniform sampler2D reflectionTexture;
uniform sampler2D refractionTexture;

uniform sampler2D dudvMap;
uniform float moveFactor;
uniform float waveStrength;

const float waterReflectivity = 2.0;

uniform sampler2D normalMap;

uniform vec3 lightColor[4];
uniform vec3 attenuation[4];

const float shineDamper = 200.0;
const float reflectivity = 10;

uniform sampler2D depthMap;

uniform float shadingLevels;

void main(void) {

    vec2 ndc = (clipSpace.xy / clipSpace.w) / 2.0 + 0.5;
    vec2 refractTexCoords = vec2(ndc.x, ndc.y);
    vec2 reflectTexCoords = vec2(ndc.x, -ndc.y);

    float near = 0.1;
    float far = 100000.0;
    // depth info in r
    float depth = texture(depthMap, refractTexCoords).r;
    float floorDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    
    depth = gl_FragCoord.z;
    float waterDistance = 2.0 * near * far / (far + near - (2.0 * depth - 1.0) * (far - near));
    float waterDepth = floorDistance - waterDistance;

    vec2 distortedTexCoords = texture(dudvMap, vec2(textureCoords.x + moveFactor, textureCoords.y)).rg * 0.1;
    distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y + moveFactor);
    vec2 totalDistortion = (texture(dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength;
    
    totalDistortion *= clamp(waterDepth/20, 0.0, 1.0);
    
    refractTexCoords += totalDistortion;
    refractTexCoords = clamp(refractTexCoords, 0.001, 0.999);
    reflectTexCoords += totalDistortion;
    reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001, 0.999);
    reflectTexCoords.y = clamp(reflectTexCoords.y, -0.999, -0.001);

    vec4 reflectColor = texture(reflectionTexture, reflectTexCoords);
    vec4 refractColor = texture(refractionTexture, refractTexCoords);
    
    vec4 normalMapColor = texture(normalMap, distortedTexCoords);
    vec3 normal = vec3(normalMapColor.r * 2.0 - 1.0, normalMapColor.b * 3, normalMapColor.g * 2.0 - 1.0);
    normal = normalize(normal);
    
    vec3 viewVector = normalize(toCameraVector);
    float refractiveFactor = dot(viewVector, normal);
    // 0.5: less reflective, 10: very reflective
    refractiveFactor = pow(refractiveFactor, waterReflectivity);
    
    vec3 totalSpecularHighlights = vec3(0.0);
    
    for (int i = 0; i < 4; i++) {
        float distance = length(fromLightVector[i]);
        float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);
    
        vec3 reflectedLight = reflect(normalize(fromLightVector[i]), normal);
        float specular = max(dot(reflectedLight, viewVector), 0.0);
        specular = pow(specular, shineDamper);
        
        float level = 0;
        if (shadingLevels > 0.1) {
            level = floor(specular * shadingLevels);
            specular = level / shadingLevels;
        }
        
        totalSpecularHighlights = totalSpecularHighlights + lightColor[i] * specular * reflectivity / attFactor;
    }
    
    out_Color = mix(reflectColor, refractColor, refractiveFactor);
    
    out_Color = mix(out_Color, vec4(0.0, 0.3, 0.5, 1.0), 0.2);
    
    totalSpecularHighlights *= clamp(waterDepth/5, 0.0, 1.0);
    
    out_Color = out_Color + vec4(totalSpecularHighlights, 0.0);
    
    out_Color.a = clamp(waterDepth/50, 0.0, 1.0);

    out_Color = mix(vec4(skyColor, 1), out_Color, visibility);

}
