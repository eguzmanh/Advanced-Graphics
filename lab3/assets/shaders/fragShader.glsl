#version 430

layout (binding=0) uniform sampler2D samp;

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec2 tc;

out vec4 fragColor;
vec4 tcolor;
vec4 lcolor;

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

uniform int hasTexture;


//vec4 tcolor;

vec3 ambient;
vec3 diffuse;
vec3 specular;


void main(void)
{	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// halfway vector varyingHalfVector was computed in the vertex shader,
	// and interpolated prior to reaching the fragment shader.
	// It is copied into variable H here for convenience later.
	vec3 H = normalize(varyingHalfVector);
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);

	// compute ADS contributions (per pixel):

	if(hasTexture == 0) {
		ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
		diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
		specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
		lcolor = vec4((ambient + diffuse + specular), 1.0);
		fragColor = lcolor;
	} else if (hasTexture == 1) {
		ambient = ((globalAmbient) + (light.ambient)).xyz;
		diffuse = light.diffuse.xyz * max(cosTheta,0.0);
		specular = light.specular.xyz * pow(max(cosPhi,0.0), 3.0);
		tcolor = texture(samp, tc);
		fragColor = 0.5*tcolor + .5*lcolor;
	}

	
	// fragColor = min((tcolor * (vec4((ambient + diffuse),1.0) + vec4(specular,0.0))), vec4(1,1,1,1));
}
