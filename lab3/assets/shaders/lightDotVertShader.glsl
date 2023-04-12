#version 430


uniform mat4 v_matrix;  // this is actually just the view matrix
uniform mat4 m_matrix;  // this is actually just the view matrix
uniform mat4 p_matrix;

uniform vec3 light_position;

void main(void) {	

	gl_Position = p_matrix  * v_matrix * vec4(light_position,1.0);
} 