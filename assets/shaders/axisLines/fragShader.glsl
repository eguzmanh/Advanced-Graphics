#version 430

out vec4 color;
in vec4 axisLineColor;

uniform mat4 v_matrix;  // this is actually just the view matrix
uniform mat4 p_matrix;

void main(void) {	
    color = axisLineColor;
}