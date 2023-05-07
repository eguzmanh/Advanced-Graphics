#version 430

layout (binding=0) uniform sampler2D samp;
layout (binding=1) uniform sampler2DShadow shadowTex;
layout (binding=2) uniform sampler3D samp3D;
layout (binding=3) uniform sampler2D heightMap;	// for height map

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 shadow_coord;
in vec2 tc;
in vec3 vertEyeSpacePos;

in vec3 originalPosition;

out vec4 fragColor;

struct PositionalLight
{	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	vec3 position;
};

struct Material
{	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;

uniform float alpha;
uniform float flipNormal;

uniform int textureStatus; // 0 for no, 1 for yes
uniform int lightStatus; // 0 for off, 1 for on

vec3 ambient;
vec3 diffuse;
vec3 specular;

vec4 tcolor;
vec4 lcolor;

float lookup(float x, float y)
{  	float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                         y * 0.001 * shadow_coord.w,
                                                         -0.01, 0.0));
	return t;
}

void main(void) {		
	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);
	
	// halfway vector varyingHalfVector was computed in the vertex shader,
	// and interpolated prior to reaching the fragment shader.
	// It is copied into variable H here for convenience later.
	vec3 H = normalize(varyingHalfVector);

	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);
	
	// compute ADS contributions (per pixel):
	
	
	float shadowFactor=0.0;
	float swidth = 2.5;
	vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor = shadowFactor / 4.0;


	vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0);	// bluish gray
	float fogStart = 0.0;
	float fogEnd = 75.0;

	// the distance from the camera to the vertex in eye space is simply the length of a
	// vector to that vertex, because the camera is at (0,0,0) in eye space.
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);

	if(textureStatus == 0) { 
		ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
		lcolor = vec4(ambient, 1.0);
		if(lightStatus == 1.0) {
				diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
				specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
				lcolor += vec4(shadowFactor * (diffuse + specular), 1.0);
		}
		//fragColor = lcolor;
		fragColor = mix(fogColor,(lcolor),fogFactor);
		fragColor = vec4(fragColor.xyz, alpha);
	} 
	else if (textureStatus == 1) { 
		ambient = ((globalAmbient) + (light.ambient)).xyz;
		lcolor = vec4(ambient, 1.0);
		if(lightStatus == 1.0) {
				diffuse = light.diffuse.xyz * max(cosTheta,0.0);
				specular = light.specular.xyz * pow(max(cosPhi,0.0), 12.0);
				lcolor += vec4(shadowFactor * (diffuse + specular), 1.0);
		}
		

		tcolor = texture(samp, tc);
		fragColor = mix(fogColor,(tcolor * lcolor),fogFactor);
		fragColor = vec4(fragColor.xyz, alpha);
		//fragColor = tcolor * lcolor;
		//fragColor = vec4(fragColor.xyz, alpha);
		//fragColor = mix(fogColor, fragColor, fogFactor);

	}

	else if (textureStatus == 2) {
		//ambient = (globalAmbient + light.ambient).xyz;
		//tcolor = texture(samp3D, originalPosition/3.0 + 0.5);

		//fragColor = 0.7 * tcolor * (globalAmbient + light.ambient + light.diffuse * max(cosTheta,0.0))
			+ 0.5 * light.specular * pow(max(cosPhi,0.0), material.shininess*3);


		ambient = ((globalAmbient) + (light.ambient)).xyz;
		lcolor = vec4(ambient, 1.0);
		if(lightStatus == 1.0) {
				diffuse = light.diffuse.xyz * max(cosTheta,0.0);
				specular = light.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3);
				lcolor += vec4(shadowFactor * (diffuse + specular), 1.0);
		}
		

		tcolor = texture(samp3D, originalPosition/3.0 + 0.5);
		fragColor = mix(fogColor,(tcolor * lcolor),fogFactor);
		fragColor = vec4(fragColor.xyz, alpha);
	}
}