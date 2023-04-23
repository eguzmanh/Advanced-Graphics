#version 430

out vec4 axisLineColor;

uniform mat4 v_matrix;  // this is actually just the view matrix
uniform mat4 p_matrix;

void main(void) {	
	const vec4 vertices[6] = vec4[6] (
		vec4(0.0, 0.0, 0.0, 1.0),
		vec4(30.0, 0.0, 0.0, 1.0),
		vec4(0.0, 0.0, 0.0, 1.0),
		vec4(0.0, 30.0, 0.0, 1.0),
		vec4(0.0, 0.0, 0.0, 1.0),
		vec4(0.0, 0.0, 30.0, 1.0)
	);

	if (gl_VertexID == 0 || gl_VertexID == 1) {
        axisLineColor = vec4(1.0, 0.0, 0.0, 1.0);
    } else if (gl_VertexID == 2 || gl_VertexID == 3) {
        axisLineColor = vec4(0.0, 1.0, 0.0, 1.0);
    } else if (gl_VertexID == 4 || gl_VertexID == 5) {
        axisLineColor = vec4(0.0, 0.0, 1.0, 1.0);
    }

	gl_Position = p_matrix * v_matrix * vertices[gl_VertexID];
} 