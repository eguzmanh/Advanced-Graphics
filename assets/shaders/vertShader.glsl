#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 vertNormal;

layout (binding=0) uniform sampler2D samp;
layout (binding=1) uniform sampler2DShadow shadowTex;
layout (binding=2) uniform sampler3D samp3D;
layout (binding=3) uniform sampler2D heightMap;	// for height map

out vec2 tc;
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;
out vec4 shadow_coord;

out vec3 originalPosition;

out vec3 vertEyeSpacePos;

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

uniform int textureStatus;
uniform int lightStatus;

void main(void)
{	varyingVertPos = (m_matrix * vec4(vertPos,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	
	//if rendering a back-face, flip the normal
	if (flipNormal < 0) varyingNormal = -varyingNormal;

	varyingHalfVector = normalize(normalize(varyingLightDir) + normalize(-varyingVertPos)).xyz;

	shadow_coord = shadowMVP * vec4(vertPos,1.0);


	// height-mapped vertex
	//vec4 p = vec4(vertPos,1.0) + vec4((vertNormal*((texture2D(heightMap,texCoord).r)/5.0f)),1.0f);


	tc = texCoord;

	originalPosition = vertPos;
	vertEyeSpacePos = (v_matrix * m_matrix * vec4(vertPos,1.0)).xyz;
	gl_Position = p_matrix * v_matrix * m_matrix * vec4(vertPos,1.0);
}

