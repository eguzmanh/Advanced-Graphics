#version 430

in vec3 tc;
in vec3 vertEyeSpacePos;
out vec4 fragColor;

uniform mat4 v_matrix;
uniform mat4 p_matrix;
layout (binding = 0) uniform samplerCube samp;



void main(void)
{
	vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0);	// bluish gray
	float fogStart = 0.0;
	float fogEnd = 75.0;

	// the distance from the camera to the vertex in eye space is simply the length of a
	// vector to that vertex, because the camera is at (0,0,0) in eye space.
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);


	
	fragColor = texture(samp,tc);
	fragColor = mix(fogColor*.3,fragColor,fogFactor);
}
