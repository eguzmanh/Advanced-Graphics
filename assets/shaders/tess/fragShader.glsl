#version 430

in vec2 tes_out;
out vec4 color;

layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 1) uniform sampler2D tex_height;
layout (binding = 2) uniform sampler2D tex_normal;

/* ---- for lighting ---- */
in vec3 varyingVertPos;
in vec3 varyingLightDir; 

in vec3 vertEyeSpacePos;


struct PositionalLight
{	vec4 ambient; vec4 diffuse; vec4 specular; vec3 position; };
struct Material
{	vec4 ambient; vec4 diffuse; vec4 specular; float shininess; };  
uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
/* ---------------------- */

vec3 calcNewNormal()
{
	vec3 normal = vec3(0,1,0);
	vec3 tangent = vec3(1,0,0);
	vec3 bitangent = cross(tangent, normal);
	mat3 tbn = mat3(tangent, bitangent, normal);
	vec3 retrievedNormal = texture(tex_normal,tes_out).xyz;
	retrievedNormal = retrievedNormal * 2.0 - 1.0;
	vec3 newNormal = tbn * retrievedNormal;
	newNormal = normalize(newNormal);
	return newNormal;
}

void main(void)
{	vec3 L = normalize(varyingLightDir);
	vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);

	vec3 N = calcNewNormal();
	
	vec3 R = normalize(reflect(-L, N));
	float cosTheta = dot(L,N);
	float cosPhi = dot(V,R);

	vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0);	// bluish gray
	float fogStart = 0.0;
	float fogEnd = 75.0;

	// the distance from the camera to the vertex in eye space is simply the length of a
	// vector to that vertex, because the camera is at (0,0,0) in eye space.
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);

	
	color = 0.5 * 
		( globalAmbient * material.ambient  +  light.ambient * material.ambient
		+ light.diffuse * material.diffuse * max(cosTheta,0.0)
		+ light.specular * material.specular * pow(max(cosPhi,0.0), material.shininess)
		) +
		0.5 *
		( texture(tex_color, tes_out)
		);
	
	color = mix(fogColor*.4,(color),fogFactor);
}