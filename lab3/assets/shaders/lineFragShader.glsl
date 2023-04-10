#version 430

out vec4 color;
in vec4 axisLineColor;

uniform mat4 mv_matrix;
uniform mat4 p_matrix;

void main(void) {	
    color = axisLineColor;
}