package a4;

import java.nio.*;
import java.util.HashMap;
import org.joml.*;
import java.lang.Math;

import java.awt.Color;

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
    private int mLoc, vLoc, pLoc, nLoc, sLoc, alphaLoc, flipLoc;; 

    private int lightLoc, globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;

    private int hasTexture, glTextureStatus; // does not need initialization because it will be set in code

    private int lightStatus;

    private int isLightOn;

    // private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	// private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

    // white light properties
	private float[] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private float[] lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
	private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		
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

    // shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];

    public Renderer() {
        vao = new int[3];
        vbo = new int[25];  
        lightPos = new float[3];

        initialLightLoc = new Vector3f(2.0f, 7f, 2f);
        currentLightPos = initialLightLoc;
        // lightStatus = true;
        isLightOn = 1;

        currVboIndex = 0;
        vals16f = Buffers.newDirectFloatBuffer(16);
        vals3f = Buffers.newDirectFloatBuffer(3);

        shaders = new HashMap<String, Integer>();

        setDefaultMaterial();
    }


    private int noiseTexture1, noiseTexture2;
	private int noiseWidth = 256;
	private int noiseHeight= 256;
	private int noiseDepth = 256;
	private double[][][] noise = new double[noiseWidth][noiseHeight][noiseDepth];
	private java.util.Random random = new java.util.Random();

    private Color c;


    public void init3DMarbleTexture() {
        generateNoise();	
		noiseTexture1 = buildNoiseTexture1();
        noiseTexture2 = buildNoiseTexture2();
    }

    public int get3DMarbleTexture1() {
        return noiseTexture1;
    }
    public int get3DMarbleTexture2() {
        return noiseTexture2;
    }

	private void fillDataArray(byte data[])
	{ double veinFrequency = 20.0;
	  double turbPower = 0.0;
	  double maxZoom =  64.0;
	  for (int i=0; i<noiseWidth; i++)
	  { for (int j=0; j<noiseHeight; j++)
	    { for (int k=0; k<noiseDepth; k++)
	      {	double xyzValue = (float)i/noiseWidth + (float)j/noiseHeight + (float)k/noiseDepth
							+ turbPower * turbulence(i,j,k,maxZoom)/256.0;

		double sineValue = logistic(Math.abs(Math.sin(xyzValue * 3.14159 * veinFrequency)));
		sineValue = Math.max(-1.0, Math.min(sineValue*1.25-0.20, 1.0));

//		c = new Color(0.33f, 0.65f, (float)Math.min(sineValue*1.5-0.25, 1.0));
		c = new Color((float)Math.min(sineValue*1.5-0.25, 1.0), 0.5f, 0.5f);
        
        
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte) c.getRed();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte) c.getGreen();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte) c.getBlue();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte) 255;
        } } } }
        
        private void fillDataArray2(byte data[])
        { double veinFrequency = 20.0;
            double turbPower = 0.0;
	  double maxZoom =  64.0;
	  for (int i=0; i<noiseWidth; i++)
	  { for (int j=0; j<noiseHeight; j++)
	    { for (int k=0; k<noiseDepth; k++)
            {	double xyzValue = (float)i/noiseWidth + (float)j/noiseHeight + (float)k/noiseDepth
                + turbPower * turbulence(i,j,k,maxZoom)/256.0;
                
                double sineValue = logistic(Math.abs(Math.sin(xyzValue * 3.14159 * veinFrequency)));
		sineValue = Math.max(-1.0, Math.min(sineValue*1.25-0.20, 1.0));

        //		c = new Color((float)Math.min(sineValue*3.5-0.25, 1.0),
        //          (float)sineValue,
				//(float)sineValue);
      //  c = new Color(0.77f,
        //        (float)Math.min(sineValue*1.5-0.25, 0.64f),
        //            0.52f
        //      );
        //c = new Color((float)Math.min(sineValue*1.5-0.25, 1.0), .76f, .58f);
        c = new Color(0.7f, 0.7f, (float)Math.min(sineValue*1.5-0.25, 1.0));
        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte) c.getRed();
        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte) c.getGreen();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte) c.getBlue();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte) 255;
	} } } }
	
	private int buildNoiseTexture1()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseWidth*noiseHeight*noiseDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

    private int buildNoiseTexture2()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseWidth*noiseHeight*noiseDepth*4];
		
		fillDataArray2(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	void generateNoise()
	{	for (int x=0; x<noiseWidth; x++)
		{	for (int y=0; y<noiseHeight; y++)
			{	for (int z=0; z<noiseDepth; z++)
				{	noise[x][y][z] = random.nextDouble();
	}	}	}	}
	
	double smoothNoise(double zoom, double x1, double y1, double z1)
	{	//get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		//neighbor values that wrap
		double x2 = x1 - 1; if (x2<0) x2 = (Math.round(noiseWidth / zoom)) - 1;
		double y2 = y1 - 1; if (y2<0) y2 = (Math.round(noiseHeight / zoom)) - 1;
		double z2 = z1 - 1; if (z2<0) z2 = (Math.round(noiseDepth / zoom)) - 1;

		//smooth the noise by interpolating
		double value = 0.0;
		value += fractX       * fractY       * fractZ       * noise[(int)x1][(int)y1][(int)z1];
		value += (1.0-fractX) * fractY       * fractZ       * noise[(int)x2][(int)y1][(int)z1];
		value += fractX       * (1.0-fractY) * fractZ       * noise[(int)x1][(int)y2][(int)z1];	
		value += (1.0-fractX) * (1.0-fractY) * fractZ       * noise[(int)x2][(int)y2][(int)z1];
				
		value += fractX       * fractY       * (1.0-fractZ) * noise[(int)x1][(int)y1][(int)z2];
		value += (1.0-fractX) * fractY       * (1.0-fractZ) * noise[(int)x2][(int)y1][(int)z2];
		value += fractX       * (1.0-fractY) * (1.0-fractZ) * noise[(int)x1][(int)y2][(int)z2];
		value += (1.0-fractX) * (1.0-fractY) * (1.0-fractZ) * noise[(int)x2][(int)y2][(int)z2];
		
		return value;
	}

	private double turbulence(double x, double y, double z, double maxZoom)
	{	double sum = 0.0, zoom = maxZoom;
		while(zoom >= 0.9)
		{	sum = sum + smoothNoise(zoom, x/zoom, y/zoom, z/zoom) * zoom;
			zoom = zoom / 2.0;
		}
		sum = 128.0 * sum / maxZoom;
		return sum;
	}

	private double logistic(double x)
	{	double k = 3.0;
		return (1.0/(1.0+Math.pow(2.718,-k*x)));
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
    
    public void setNeutralWhiteMaterial() {
        matAmb = Utils.whiteAmbient();
        matDif = Utils.whiteDiffuse();
        matSpe = Utils.whiteSpecular();
        matShi = Utils.whiteShininess();
    }

    public void setDefaultMaterial() { setNeutralWhiteMaterial(); }

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
        shaders.put("mainShadowShader", Utils.createShaderProgram("assets/shaders/vertShadowShader.glsl", "assets/shaders/fragShadowShader.glsl"));
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

    public void setupShadowBuffers(int scx, int scy) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = scx;
		scSizeY = scy;
	
		gl.glGenFramebuffers(1, shadowBuffer, 0);
	
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }
    // ****************************** Actions ******************************

    public void useMainShadowShader() { 
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(shaders.get("mainShadowShader"));
        sLoc = gl.glGetUniformLocation(shaders.get("mainShadowShader"), "shadowMVP");
    }

    public void useMainShader() { 
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUseProgram(shaders.get("mainShader"));
        mLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "m_matrix");
		vLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "v_matrix");
		pLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "p_matrix");
		nLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "norm_matrix");
        sLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "shadowMVP");
        alphaLoc = gl.glGetUniformLocation(shaders.get("mainShader"), "alpha");
		flipLoc = gl.glGetUniformLocation(shaders.get("mainShader") , "flipNormal");
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

    
    public void renderAlphaTexturedMaterialObject(int vboObjId,int numVertices, int vboTxId, int texture, int vboNId){
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform1i(glTextureStatus, 2);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboTxId]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNId]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_3D, texture);

        gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		gl.glBlendEquation(GL_FUNC_ADD);

		gl.glEnable(GL_CULL_FACE);
		
		gl.glCullFace(GL_FRONT);
        gl.glUniform1f(alphaLoc, 0.3f);
        gl.glUniform1f(flipLoc, -1.0f);
		// gl.glProgramUniform1f(renderingProgram, alphaLoc, 0.3f);
		// gl.glProgramUniform1f(renderingProgram, flipLoc, -1.0f);
		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
		
		gl.glCullFace(GL_BACK);
        gl.glUniform1f(alphaLoc, 0.5f);
        gl.glUniform1f(flipLoc, 1.0f);
		// gl.glProgramUniform1f(renderingProgram, alphaLoc, 0.7f);
		// gl.glProgramUniform1f(renderingProgram, flipLoc, 1.0f);
		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);

		gl.glDisable(GL_BLEND);
    }

    // public void renderAlphaTexturedMaterialObject() {}

    /** Bind objects with a texture */
    public void renderTexturedMaterialWorldObject(int vboObjId,int numVertices, int vboTxId, int texture, int vboNId) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform1i(glTextureStatus, 2);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboTxId]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNId]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_3D, texture);
        
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);


		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}

	public void renderWorldObject(int vboObjId,int numVertices, int vboTxId, int texture, int vboNId) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform1i(glTextureStatus, 1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboTxId]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNId]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texture);
        
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
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


		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}

     /** Bind objects with a texture */
	public void renderWorldObjectFirst(int vboObjId,int numVertices, int vboTxId, int texture, int vboNId) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform1i(glTextureStatus, 1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboTxId]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNId]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
        
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, texture);
        
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);


		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}

    /** Bind objects without a texture */
	public void renderWorldObjectFirst(int vboObjId, int numVertices, int vboNId) {
        GL4 gl = (GL4) GLContext.getCurrentGL();

        gl.glUniform1i(glTextureStatus, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboObjId]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNId]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

        gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}


    public void setMainShaderUniVars(Matrix4f mMat, Matrix4f vMat, Matrix4f pMat, Matrix4f nMat, Matrix4f sMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        // gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals16f));
        gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals16f));
        gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals16f));
        gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals16f));
        gl.glUniformMatrix4fv(nLoc, 1, false, nMat.get(vals16f));
        gl.glUniformMatrix4fv(sLoc, 1, false, sMat.get(vals16f));
        gl.glUniform1f(alphaLoc, 1.0f);
        gl.glUniform1f(flipLoc, 1.0f);
        

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
    
    public void setSUniformVar(Matrix4f sMat) {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniformMatrix4fv(sLoc, 1, false, sMat.get(vals16f));
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

        gl.glColorMask(true, true, true, true);
		gl.glClear(GL_DEPTH_BUFFER_BIT);	
		gl.glClearColor(0.7f, 0.8f, 0.9f, 1.0f); // background fog color is bluish-grey
		gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    public void anaglyphLeftColorMask() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glColorMask(true, false, false, false);
    }

    public void anaglyphRightColorMask() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glColorMask(false, true, true, false);
    }

    public int getCurrVBOIndex() { return currVboIndex; }
    
    // // private dir created for a direction change
    // private int dir = 1;
    public void setupLights(float elapsedSpeed) {
        // amt = elapsedSpeed;
        // System.out.prin

        // currentLightPos.set(currentLightPos);
        installLights();
    }

    public void prepPass1GLData() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
		gl.glPolygonOffset(3.0f, 5.0f);		//  shadow artifacts
    }

    public void prepPass2GLData() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
    }

    public Vector3f getCurrentLight() { return currentLightPos; }
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
    public void setCurrentLightPositionXYZ() {}

    public void setLightStatus() {
        GL4 gl = (GL4) GLContext.getCurrentGL();
        gl.glUniform1i(lightStatus, isLightOn);
    }
    
    public void lightOn() { isLightOn = 1; }
    
    public void lightOff() { isLightOn = 0; }

    public void toggleLight() { isLightOn = isLightOn == 0 ? 1 : 0; }

    public int lightStatus() { return isLightOn; }

    public boolean useTextureInShader() { return hasTexture != 0; }

    public void setHasTextureFlag(int flag) {
        if (flag == 0 || flag == 1) { hasTexture = flag;}
    }

    // private void installLightProps() {

    // }
    // always set the materials, even if it is a very dim white for textured objs
    private void installLights() {	
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
