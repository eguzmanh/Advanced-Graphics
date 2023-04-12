package a3;

import java.nio.*;
import java.util.HashMap;
import org.joml.*;
import java.lang.Math;

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
    private int mLoc, vLoc, pLoc, nLoc; 

    private int lightLoc, globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;

    private int hasTexture, glTextureStatus; // does not need initialization because it will be set in code

    private int lightStatus;

    private int isLightOn;

    // white light properties
	float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
	float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		
	// gold material
	float[] matAmb;
	float[] matDif;
	float[] matSpe;
	float matShi;

    float[] goldMatAmb = Utils.goldAmbient();
	float[] goldMatDif = Utils.goldDiffuse();
	float[] goldMatSpe = Utils.goldSpecular();
    float goldMatShi = Utils.goldShininess();

    float[] amethystMatAmb = Utils.amethystAmbient();
	float[] amethystMatDif = Utils.amethystDiffuse();
	float[] amethystMatSpe = Utils.amethystSpecular();
    float amethystMatShi = Utils.amethystShininess();
    
    private Vector3f initialLightLoc, currentLightPos;
    private float[] lightPos;
    
    private FloatBuffer vals16f, vals3f;

    private float amt;

    public Renderer() {
        vao = new int[3];
        vbo = new int[20];  
        lightPos = new float[3];

        initialLightLoc = new Vector3f(0.0f, 10.0f, 0.0f);
        currentLightPos = initialLightLoc;
        // lightStatus = true;
        isLightOn = 1;

        currVboIndex = 0;
        vals16f = Buffers.newDirectFloatBuffer(16);
        vals3f = Buffers.newDirectFloatBuffer(3);
        shaders = new HashMap<String, Integer>();
        setAmethystMaterial();
    }


    public void setAmethystMaterial() {
        matAmb = Utils.amethystAmbient();
        matDif = Utils.amethystDiffuse();
        matSpe = Utils.amethystSpecular();
        matShi = Utils.amethystShininess();
    }

    public void setGoldMaterial() {
        matAmb = Utils.goldAmbient();
        matDif = Utils.goldDiffuse();
        matSpe = Utils.goldSpecular();
        matShi = Utils.goldShininess();
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

    public void bindWorldObject(WorldObject wo, float[] vertices) {
        wo.setVBOIndex(currVboIndex);
        bindVBO(vertices);
    }

    private void createRenderingPrograms() {
        shaders.put("mainShader", Utils.createShaderProgram("assets/shaders/vertShader.glsl", "assets/shaders/fragShader.glsl"));
        shaders.put("axisLineShader", Utils.createShaderProgram("assets/shaders/lineVertShader.glsl", "assets/shaders/lineFragShader.glsl"));
        shaders.put("cubeMapShader", Utils.createShaderProgram("assets/shaders/cubeMapVertShader.glsl", "assets/shaders/cubeMapFragShader.glsl"));
        shaders.put("lightDotShader", Utils.createShaderProgram("assets/shaders/lightDotVertShader.glsl", "assets/shaders/lightDotFragShader.glsl"));
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
        mLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "m_matrix");
		vLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "v_matrix");
		pLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "p_matrix");
		nLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "norm_matrix");

        // include a texture flag in order to use the correct material light properties
        glTextureStatus = gl.glGetUniformLocation(shaders.get("mainShader"), "textureStatus");
        lightStatus = gl.glGetUniformLocation(shaders.get("mainShader"), "lightStatus");
    }

    public void useCubeMapShader() { 
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(shaders.get("cubeMapShader"));
		vLoc = gl.glGetUniformLocation(shaders.get("cubeMapShader"), "v_matrix");
		pLoc = gl.glGetUniformLocation(shaders.get("cubeMapShader"), "p_matrix");
    }

    public void useLineShader() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // * Generate the axis line with a hard coded vertex file
        gl.glUseProgram(shaders.get("axisLineShader"));

        // mLoc = gl.glGetUniformLocation(shaders.get("axisLineShader"), "m_matrix");
		vLoc = gl.glGetUniformLocation(shaders.get("axisLineShader"), "v_matrix");
		pLoc = gl.glGetUniformLocation(shaders.get("axisLineShader"), "p_matrix");
    }

    public void useLightDotShader() {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // * Generate the axis line with a hard coded vertex file
        gl.glUseProgram(shaders.get("lightDotShader"));

        // mLoc = gl.glGetUniformLocation(shaders.get("axisLineShader"), "m_matrix");
        lightLoc = gl.glGetUniformLocation(shaders.get("lightDotShader"), "light_position");
		vLoc = gl.glGetUniformLocation(shaders.get("lightDotShader"), "v_matrix");
		pLoc = gl.glGetUniformLocation(shaders.get("lightDotShader"), "p_matrix");
    }

    public void enableCubeMap() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
    }
    
    public void renderCubeMap(int vboObjId,int numVertices, int texture) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, texture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);

		gl.glEnable(GL_DEPTH_TEST);
        gl.glFrontFace(GL_CW);	     // only need GL_CCW for the skybox (reset for world items)
        gl.glDisable(GL_CULL_FACE);
		
    }
    
    public void renderAxisLines(Matrix4f vMat, Matrix4f pMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        // pass in view as mvLoc to display the lines at the origin
        // gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals16f));
        
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals16f));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals16f));

        gl.glDrawArrays(GL_LINES, 0, 6);
	}

    public void renderLightDot(Matrix4f vMat, Matrix4f pMat) {
        if(isLightOn == 0) return;
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform3fv(lightLoc, 1, currentLightPos.get(vals3f));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals16f));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals16f));

        gl.glPointSize(50.0f);
        gl.glDrawArrays(GL_POINTS, 0, 1);
    }
    
    /** Bind objects with a texture */
	public void renderWorldObject(int vboObjId,int numVertices, int vboTxId, int texture, int vboNId) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform1i(glTextureStatus, 1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboTxId]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texture);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNId]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}

	/** Bind objects without a texture */
	public void renderWorldObject(int vboObjId, int numVertices, int vboNId) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform1i(glTextureStatus, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNId]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}

    public void setMVPUniformVars(Matrix4f mMat, Matrix4f vMat, Matrix4f pMat, Matrix4f nMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        // gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals16f));
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals16f));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals16f));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals16f));
        gl.glUniformMatrix4fv(nLoc, 1, false, nMat.get(vals16f));
    }

    public void setMVUniformVar(Matrix4f mMat,Matrix4f vMat, Matrix4f nMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals16f));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals16f));
        gl.glUniformMatrix4fv(nLoc, 1, false, nMat.get(vals16f));
    }

    public void setVPUniformVar(Matrix4f vMat, Matrix4f pMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals16f));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals16f));
    }
    

    public void setMVStackUniformVar(Matrix4f vMat, Matrix4fStack mStack, Matrix4f nMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(mLoc, 1, false, mStack.get(vals16f));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals16f));
        gl.glUniformMatrix4fv(nLoc, 1, false, nMat.get(vals16f));
    }

    public void setPUniformVar(Matrix4f pMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals16f));
    }

    public void clearGL() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public int getCurrVBOIndex() { return currVboIndex; }
    
    // private dir created for a direction change
    private int dir = 1;
    public void setupLights(float elapsedSpeed) {
        if (isLightOn == 0) { return; } 
        amt = elapsedSpeed;
        // System.out.prin
        
        // float x = currentLightPos.x;
        // float y = currentLightPos.y;
        // float z = currentLightPos.z;
        // float x = currentLightPos.x + amt * dir;
        // float y = currentLightPos.y + amt * dir;
        // float z = currentLightPos.z + amt * dir;
        // float currVal = z; // set to x, y, z for direction changes
        
        // if(currVal >= 10.0f) dir = -1;
        // if(currVal <= 0.0f) dir = 1;

        // currentLightPos.set(currentLightPos);
        installLights();
    }

    public float getCurrentLightX() { return currentLightPos.x; }
    public float getCurrentLightY() { return currentLightPos.y; }
    public float getCurrentLightZ() { return currentLightPos.z; }
    public void setCurrentLightPositionX(float x) { currentLightPos.set(x, currentLightPos.y, currentLightPos.z); }
    public void setCurrentLightPositionY(float y) { currentLightPos.set(currentLightPos.x, y, currentLightPos.z); }
    public void setCurrentLightPositionZ(float z) { currentLightPos.set(currentLightPos.x, currentLightPos.y, z); }

    public void lightPositionAdd(float x, float y, float z) { currentLightPos.add(x,y,z); }
    public void addCurrentLightPositionY(float y) { currentLightPos.add(0f,y,0f); }

    public void setCurrentLightPositionXY(float x, float y) { currentLightPos.set(x, y, currentLightPos.z); }
    public void setCurrentLightPositionXZ(float x, float z) { currentLightPos.set(x, currentLightPos.y, z); }

    public void setCurrentLightPositionXYZ() {

    }

    public void setLightStatus() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniform1i(lightStatus, isLightOn);
    }
    
    public void lightOn() { isLightOn = 1; }
    
    public void lightOff() { isLightOn = 0; }

    public void toggleLight() { isLightOn = isLightOn == 0 ? 1 : 0; }

    public int lightStatus() { return lightStatus; }

    public boolean useTextureInShader() { return hasTexture != 0; }

    public void setHasTextureFlag(int flag) {
        if (flag == 0 || flag == 1) { hasTexture = flag;}
    }

    private void installLights()
	{	
        if (isLightOn == 0) return;

        GL4 gl = (GL4) GLContext.getCurrentGL();
		
		lightPos[0]=currentLightPos.x; lightPos[1]=currentLightPos.y; lightPos[2]=currentLightPos.z;
		
		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "globalAmbient");
		ambLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "light.ambient");
		diffLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "light.diffuse");
		specLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "light.specular");
		posLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "light.position");
		mambLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "material.specular");
		mshiLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "material.shininess");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(shaders.get("mainShader"), globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(shaders.get("mainShader"), ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(shaders.get("mainShader"), diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(shaders.get("mainShader"), specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(shaders.get("mainShader"), posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(shaders.get("mainShader"), mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(shaders.get("mainShader"), mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(shaders.get("mainShader"), mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(shaders.get("mainShader"), mshiLoc, matShi);
	}
}
