#version 430

layout (binding=0) uniform sampler2D samp;
layout (binding=1) uniform sampler2DShadow shadowTex;

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 shadow_coord;
in vec2 tc;
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

uniform int textureStatus; // 0 for no, 1 for yes
uniform int lightStatus; // 0 for off, 1 for on

vec3 ambient;
vec3 diffuse;
vec3 specular;

vec4 tcolor;
vec4 lcolor;


float lookup(float ox, float oy) {
	float t = textureProj(shadowTex, shadow_coord + vec4(ox * 0.001 * shadow_coord.w, oy * 0.001 * shadow_coord.w, -0.01, 0.0));
	return t;
}

void main(void) {	

	// PCF filtering being added into the program
	float shadowFactor = 0.0;
	float swidth = 2.5;
	vec2 offset = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + offset.x, 1.5*swidth-offset.y);
	shadowFactor += lookup(-1.5*swidth + offset.x, -0.5*swidth-offset.y);
	shadowFactor += lookup(0.5*swidth + offset.x, 1.5*swidth-offset.y);
	shadowFactor += lookup(0.5*swidth + offset.x, -0.5*swidth-offset.y);
	shadowFactor = shadowFactor / 4.0;


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
	
	//float notInShadow = textureProj(shadowTex, shadow_coord);

	if(textureStatus == 0) { 
		ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
		if(lightStatus == 1.0) {
				diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
				specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
				lcolor = vec4(ambient + shadowFactor*(diffuse + specular), 1.0);
		} else {
			lcolor = vec4(ambient, 1.0);
		}
		fragColor = lcolor;
	} 
	else if (textureStatus == 1) { 
		ambient = ((globalAmbient) + (light.ambient)).xyz;
		if(lightStatus == 1.0) {
				diffuse = light.diffuse.xyz * max(cosTheta,0.0);
				specular = light.specular.xyz * pow(max(cosPhi,0.0), 1.0);
				lcolor += vec4(ambient + shadowFactor*(diffuse + specular), 1.0);
		} else {
			lcolor = vec4(ambient, 1.0);
		}
		
		tcolor = texture(samp, tc);
		fragColor = tcolor * lcolor;

	}
}
