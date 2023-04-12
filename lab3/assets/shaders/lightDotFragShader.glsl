#version 430

out vec4 color;

uniform mat4 v_matrix;  // this is actually just the view matrix
uniform mat4 m_matrix;  // this is actually just the view matrix
uniform mat4 p_matrix;

uniform vec3 light_position;

void main(void) {	
    // color = vec4(1.0, 0.85, 0.34, 1.0); // sun-like red-yellow  
    color = vec4(1.0, 0.885, 0.114, 1.0); // sun-like yellow 
}