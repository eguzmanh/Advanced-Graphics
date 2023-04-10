package a3;

import java.nio.*;
import java.util.HashMap;
import org.joml.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.GLContext;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.awt.GLCanvas;

import static com.jogamp.opengl.GL4.*;




public class Renderer {
    private HashMap<String, Integer> shaders;

	public int vao[];
	public int vbo[];
    private int currVboIndex;
    private int mvLoc, pLoc; 

    private FloatBuffer vals;

    public Renderer() {
        vao = new int[3];
        vbo = new int[15];  
        currVboIndex = 0;
        vals = Buffers.newDirectFloatBuffer(16);
        shaders = new HashMap<String, Integer>();
    }

    // ****************************** Initialization ******************************
    public void createShaders() {
        createRenderingPrograms();
    }

    // * Binding VAO and VBO
    public void bindVertextAttributeData() {
        setUpVertexArrayObjects();
        setUpVertexBufferObjects();
    }

    public void bindTexturedWorldObject(WorldObject wo, float[] vertices, float[] txCoords, float[] normals) {
        wo.setVBOIndex(currVboIndex);
        bindVBO(vertices);
        wo.setVBOTxIndex(currVboIndex);
        bindVBO(txCoords);
        wo.setVBONIndex(currVboIndex);
        bindVBO(normals);
    }

    public void bindTexturedWorldObject(WorldObject wo, float[] vertices, float[] txtCoords) {
        wo.setVBOIndex(currVboIndex);
        bindVBO(vertices);
        wo.setVBOTxIndex(currVboIndex);
        bindVBO(txtCoords);  // texture or normals
    }

    public void bindWorldObjectWNormals(WorldObject wo, float[] vertices, float[] normals) {
        wo.setVBOIndex(currVboIndex);
        bindVBO(vertices);
        wo.setVBONIndex(currVboIndex);
        bindVBO(normals);  // texture or normals
    }

    // public void bindWorldObject(WorldObject wo, float[] vertices, float[] normals) {
    //     wo.setVBOIndex(currVboIndex);
    //     bindVBO(vertices);
    //     wo.setVBOIndex(currVboIndex);
    //     bindVBO(normlas);
    // }

    public void bindWorldObject(WorldObject wo, float[] vertices) {
        wo.setVBOIndex(currVboIndex);
        bindVBO(vertices);
    }


    private void createRenderingPrograms() {
        shaders.put("mainShader", Utils.createShaderProgram("assets/shaders/vertShader.glsl", "assets/shaders/fragShader.glsl"));
        shaders.put("axisLineShader", Utils.createShaderProgram("assets/shaders/lineVert.glsl", "assets/shaders/lineFrag.glsl"));
        // mainShader = Utils.createShaderProgram("assets/shaders/vertShader.glsl", "assets/shaders/fragShader.glsl");
		// axisLineShader = Utils.createShaderProgram("assets/shaders/lineVert.glsl", "assets/shaders/lineFrag.glsl");
    }

    private void setUpVertexArrayObjects() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
    }

    private void setUpVertexBufferObjects() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glGenBuffers(vbo.length, vbo, 0);
    }

    private void bindVBO(float[] points) {
        // System.out.println("currVboIndex: " + currVboIndex);
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[currVboIndex++]);
		FloatBuffer buf = Buffers.newDirectFloatBuffer(points);
		gl.glBufferData(GL_ARRAY_BUFFER, buf.limit()*4, buf, GL_STATIC_DRAW);

        // currVboIndex += 1;
        // System.out.println("currVboIndex: " + currVboIndex);
    }


    // ****************************** Actions ******************************

    public void useMainShader() { 
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(shaders.get("mainShader"));
        mvLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "mv_matrix");
		pLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "p_matrix");
    }
    
    /** Bind objects with a texture */
	public void drawWorldObject(int vboObjId,int numVertices, int vboTxId, int texture) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboTxId]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}

	/** Bind objects without a texture */
	public void drawWorldObject(int vboObjId, int numVertices) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}

    public void renderAxisLines(Matrix4f mvMat, Matrix4f pMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

		// * Generate the axis line with a hard coded vertex file
        gl.glUseProgram(shaders.get("axisLineShader"));

        // pass in view as mvLoc to display the lines at the origin
        gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
        gl.glDrawArrays(GL_LINES, 0, 6);
	}

    public void setMPUniformVars(Matrix4f mvMat, Matrix4f pMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
    }

    public void setMUniformVar(Matrix4f mvMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
    }

    public void setMUniformVar(Matrix4fStack mvStack) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
    }

    public void setPUniformVar(Matrix4f pMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
    }

    public void clearGL() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public int getCurrVBOIndex() { return currVboIndex; }
}
