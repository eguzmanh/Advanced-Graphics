#version 430

in vec3 vNormal;
in vec3 vVertPos;
out vec4 fragColor;

uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
layout (binding = 0) uniform samplerCube t;

void main(void)
{
	vec3 r = -reflect(normalize(-vVertPos), normalize(vNormal));
	fragColor = texture(t,r);
}