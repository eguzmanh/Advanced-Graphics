package a4;

// import a3.models.*;

import java.io.*;
import java.lang.Math;
import java.nio.*;

import javax.naming.ldap.Rdn;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;

import com.jogamp.opengl.util.texture.*;

import a4.shapes.*;

import com.jogamp.common.nio.Buffers;
import org.joml.*;

import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseAdapter;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


public class Code extends JFrame implements GLEventListener, KeyListener, MouseMotionListener, MouseWheelListener  {	
	private GLCanvas myCanvas;
	
	private Camera camera;
	private Renderer renderer;

	private int numObjects;

	private double lastFrameTime, currFrameTime, elapsedTime, startTime, elapsedStackTime;

	private float elapsedTimeOffset, elapsedTimeStackOffset;
	private float aspect;

	private boolean displayAxisLines;

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat;  // perspective matrix
	private Matrix4f vMat;  // view matrix
	private Matrix4f mMat;  // model matrix
	private Matrix4f invTrMat; // inverse-transpose
	private Matrix4fStack mStack;

	// // shadow stuff
	// private int scSizeX, scSizeY;
	// private int [] shadowTex = new int[1];
	// private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat;
	private Matrix4f lightPmat;
	private Matrix4f shadowMVP1;
	private Matrix4f shadowMVP2;
	private Matrix4f b;
	// ! Used for lookat for now, but need to implement look at camera method
	private Vector3f origin;
	private Vector3f up;

	
	private ImportedModel dolphinObj, waterTankObj;
	private Cube skyboxCube;
	private Dolphin dol;
	private WaterTank waterTank;
	private Cube waterTankBox1;
	
	private int dolTexture, skyboxTexture, waterTankTexture;

	
	private Vector3f currObjLoc;
	private float x,y,z;

	private int lastX, lastY, lastVarsSet = 0;


	// **************** Constructor(s) ****************************
	public Code() {	
		numObjects = 13;
		displayAxisLines = true;
		renderer = new Renderer();

		initTimeFrames();
		styleJFrame();
		initMainComponents();
		initMatrices();
		initCanvas();
		initAnimator();
	}


	// ************************* Initialization *************************
	/** Inits the necessary components to run the java and shader programs */
	public void init(GLAutoDrawable drawable) {	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		renderer.createShaders();
		setupShadowBuffers();
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f
		);
		
		setPerspective();
		loadObjects();
		loadTextures();
		buildWorldObjects();
		
		renderer.bindVertextAttributeData();
		
		bindWorldObjects();
	}
	
	private void setupShadowBuffers() {
		renderer.setupShadowBuffers(myCanvas.getWidth(), myCanvas.getHeight());
	}
	private void styleJFrame() {
		setTitle("CSC 155 - LAB 2");
		setSize(1100, 800);
	}

	private void initMainComponents() {
		camera = new Camera();
	}

	private void initMatrices() {
		currObjLoc = new Vector3f();

		// Matrices for the model view stack
		pMat = new Matrix4f(); 
		vMat = new Matrix4f();  
		mMat = new Matrix4f();  
		invTrMat = new Matrix4f(); 
		mStack = new Matrix4fStack(7);

		lightVmat = new Matrix4f();
		lightPmat = new Matrix4f();
		shadowMVP1 = new Matrix4f();
		shadowMVP2 = new Matrix4f();
		b = new Matrix4f();
		origin = new Vector3f(0.0f, 0.0f, 0.0f);
		up = new Vector3f(0.0f, 1.0f, 0.0f);
	}

	private void initCanvas() {
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);
		myCanvas.addMouseWheelListener(this);
		myCanvas.addMouseMotionListener(this);
		getContentPane().add(myCanvas);
		this.setVisible(true);
	}

	private void initAnimator() {
		Animator animator = new Animator(myCanvas);
		animator.start();
	}
	
	private void initTimeFrames() {
		lastFrameTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		elapsedTime = 0.0;
	}
	
	private void setPerspective() {
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		
		lightVmat.identity().setLookAt(renderer.getCurrentLight(), origin, up);	// vector from light to origin
		lightPmat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}

	private void loadObjects() {
		dolphinObj = new ImportedModel("assets/models/dolphinHighPoly.obj");
		waterTankObj = new ImportedModel("assets/models/waterTank.obj");
	}

	// refer to readme for source explanations
	private void loadTextures() {
		dolTexture =  Utils.loadTexture("assets/textures/Dolphin_HighPolyUV.png");
		skyboxTexture = Utils.loadCubeMap("assets/cubeMaps/space/red");
		waterTankTexture = Utils.loadTexture("assets/textures/water-tank/colour.png");
	}

	private void buildWorldObjects() {	
		skyboxCube = new Cube("cube");
		waterTankBox1 = new Cube("cube");
		dol = new Dolphin("dolphin", dolphinObj);
		waterTank = new WaterTank("waterTank", waterTankObj);
	}

	private void bindWorldObjects() {
		renderer.bindWorldObject(skyboxCube, skyboxCube.getVertices());
		renderer.bindTexturedWorldObject(dol, dol.getVertices(), dol.getTextureCoordinates(), dol.getNormals());
		renderer.bindTexturedWorldObject(waterTank, waterTank.getVertices(), waterTank.getTextureCoordinates(), waterTank.getNormals());
		renderer.bindWorldObjectWNormals(waterTankBox1, waterTankBox1.getVertices(), waterTankBox1.getNormals());
	}


	// ************************* Runtime Actions *************************
	/** Renders on every frame and does actions */
	public void display(GLAutoDrawable drawable) {
		renderer.clearGL();
		
		upateElapsedTimeInfo();
		setPerspective();

		// * View Matrix from Camera
		vMat.set(camera.getViewMatrix());

		// System.out.println(renderer.lightStatus());
		
		renderer.useCubeMapShader();
		renderSkybox();
		
		renderer.useLineShader();
		renderAxisLines();

		renderer.useLightDotShader();
		renderLightDot();

		
		renderer.prepPass1GLData();
		renderer.useMainShadowShader();
		// passOne();
		renderWorldObjectsP1();		

		renderer.prepPass2GLData();
		renderer.useMainShader();
		
		renderer.setLightStatus();
		// renderer.prepPass2GLData();

		renderWorldObjectsP2();		
	}

	private void renderSkybox() {
		renderer.setVPUniformVar(vMat, pMat);
		renderer.renderCubeMap(skyboxCube.getVBOIndex(), skyboxCube.getNumVertices(), skyboxTexture);
	}

	private void renderAxisLines() {
		if (displayAxisLines) {
			renderer.renderAxisLines(vMat, pMat);
		}
	}
	
	private void renderLightDot() {
		renderer.renderLightDot(vMat, pMat);
	}
	

	// TODO: change the renderer and display so that objects go through both passOne and passTwo 
	// TODO: create an update for eadch object separately so that updates to an object's position only occurs once per round


	private void renderWorldObjectsP2() {

		// * Dolphin Object
		renderer.setupLights(elapsedTimeOffset);
		mMat.identity();
		updateDolphin();

		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);

		renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP2);
		renderer.renderWorldObject(dol.getVBOIndex(), dol.getNumVertices(), dol.getVBOTxIndex(), dolTexture, dol.getVBONIndex());



		renderer.setupLights(elapsedTimeOffset);
		mMat.identity();
		updateWaterTank();

		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);

		renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP2);
		renderer.renderWorldObject(waterTank.getVBOIndex(), waterTank.getNumVertices(), waterTank.getVBOTxIndex(), waterTankTexture, waterTank.getVBONIndex());
	


		renderer.setGoldMaterial();
		renderer.setupLights(elapsedTimeOffset);
		
		mMat.identity();
		updateWaterTankBox1();

		renderer.setupLights(elapsedTimeOffset);
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP2);
		renderer.renderWorldObject(waterTankBox1.getVBOIndex(), waterTankBox1.getNumVertices(), waterTankBox1.getVBONIndex());
		// ***** Using different shaderPrograms *****
		// mMat.identity();
	}

	private void renderWorldObjectsP1() {

		// * Dolphin Object
		// renderer.setupLights(elapsedTimeOffset);
		mMat.identity();
		updateDolphin();

		// mMat.invert(invTrMat);
		// invTrMat.transpose(invTrMat);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);

		// renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP1);
		renderer.setSUniformVar(shadowMVP1);
		renderer.renderWorldObject(dol.getVBOIndex(), dol.getNumVertices(), dol.getVBOTxIndex(), dolTexture, dol.getVBONIndex());



		// renderer.setupLights(elapsedTimeOffset);
		mMat.identity();
		updateWaterTank();

		// mMat.invert(invTrMat);
		// invTrMat.transpose(invTrMat);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);

		// renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP1);
		renderer.setSUniformVar(shadowMVP1);
		renderer.renderWorldObject(waterTank.getVBOIndex(), waterTank.getNumVertices(), waterTank.getVBOTxIndex(), waterTankTexture, waterTank.getVBONIndex());
	


		// renderer.setGoldMaterial();
		renderer.setupLights(elapsedTimeOffset);
		
		mMat.identity();
		updateWaterTankBox1();

		// renderer.setupLights(elapsedTimeOffset);
		// mMat.invert(invTrMat);
		// invTrMat.transpose(invTrMat);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		
		// renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP1);
		renderer.setSUniformVar(shadowMVP1);
		renderer.renderWorldObject(waterTankBox1.getVBOIndex(), waterTankBox1.getNumVertices(), waterTankBox1.getVBONIndex());
		// ***** Using different shaderPrograms *****
		// mMat.identity();
	}

	/** Multiply the View and Model matrices to the Model-View identity matrix */
	private void updateMMatrix() {
		renderer.setupLights(elapsedTimeOffset);
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		renderer.setMVPUniformVars(mMat, vMat, pMat, invTrMat);
	}

	/* Objects Used */
	private void updateDolphin() {
		dol.update(elapsedTimeOffset);
		
		
		currObjLoc = dol.getLocation();

		// if (currObjLoc.x >= 15f) { mMat.rotate(135, 0.0f, 1.0f, 0.0f); }
		
		// x = currObjLoc.x() + elapsedTimeOffset * dol.getXDirection();
		x = currObjLoc.x();
		y = currObjLoc.y();
		// z = currObjLoc.z() + elapsedTimeOffset * dol.getZDirection();
		z = currObjLoc.z();
		
		dol.setLocation(x, y, z);
		
		mMat.translation(x, y, z);
		mMat.scale(2.0f, 2.0f, 2.0f);
		mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);
		// if (dol.getXDirection() < 0) { mMat.rotate(-1.0f, 0.0f, 1.0f, 0.0f); }
		// else {mMat.rotate(2f, 0.0f, 1.0f, 0.0f);}

		// updateMMatrix();
		// renderer.setupLights(elapsedTimeOffset);
		// mMat.invert(invTrMat);
		// invTrMat.transpose(invTrMat);

		// shadowMVP2.identity();
		// shadowMVP2.mul(b);
		// shadowMVP2.mul(lightPmat);
		// shadowMVP2.mul(lightVmat);
		// shadowMVP2.mul(mMat);
		
		// renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP2);
		// renderer.renderWorldObject(dol.getVBOIndex(), dol.getNumVertices(), dol.getVBOTxIndex(), dolTexture, dol.getVBONIndex());
	}

	private void updateWaterTank() {
		waterTank.update(elapsedTimeOffset);
		currObjLoc = waterTank.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		// z = currObjLoc.z() + elapsedTimeOffset * waterTank.getZDirection();
		z = currObjLoc.z();

		waterTank.setLocation(x, y, z);
		
		mMat.translation(x, y, z);
		mMat.scale(0.5f, 0.5f, 0.4f);
		mMat.rotate((float)Math.toRadians(90.0f), 0f,1f,0f);
		
		// if (boat.getZDirection() < 0) mMat.rotate(135, 0.0f, 1.0f, 0.0f);
		// renderer.setNeutralWhiteMaterial();
		// renderer.setupLights(elapsedTimeOffset);
		// updateMMatrix();

		// renderer.renderWorldObject(waterTank.getVBOIndex(), waterTank.getNumVertices(), waterTank.getVBOTxIndex(), waterTankTexture, waterTank.getVBONIndex());
	}

	private void updateWaterTankBox1() {
		waterTankBox1.update(elapsedTimeOffset, 0.0f, 5.0f); // pyramid 2

		currObjLoc = waterTankBox1.getLocation();
		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();

		waterTankBox1.setLocation(x,y,z);

		mMat.translation(x-3.0f, y-1.0f, z-1f);	
		// mMat.rotate(waterTankBox1.getRotationAngle(), 0.0f, 1.0f, 0.0f);
		mMat.scale(2f, 1f, 2f);
		
		// renderer.setGoldMaterial();
		// renderer.setupLights(elapsedTimeOffset);
		// updateMMatrix();

		// renderer.renderWorldObject(waterTankBox1.getVBOIndex(), waterTankBox1.getNumVertices(), waterTankBox1.getVBONIndex());
	}


	// Deals with elapsed time and ensure that the values stay close
	private void upateElapsedTimeInfo() {
		currFrameTime = System.currentTimeMillis();

		elapsedTime += (currFrameTime - lastFrameTime) / 1000f;
		elapsedTimeOffset = (float) (currFrameTime - lastFrameTime);

		// values passed in to the objects in order to handle movement correctly
		elapsedTimeOffset *= 0.001f;
		
		// value should be below 1.0
		if(elapsedTimeOffset > 1.0f) elapsedTimeOffset *= 0.005;


		lastFrameTime = currFrameTime;
	}
 

	// ************************* Overrides *************************
	@Override
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	
	@Override
	public void keyPressed(KeyEvent e) {	
		int evtKeyCode = e.getKeyCode();
		switch (evtKeyCode) {
			case KeyEvent.VK_ESCAPE:
				System.out.println("Esc key pressed");
				shutdownAction();
				break;
			case KeyEvent.VK_SPACE:
				System.out.println("Space key pressed");
				toggleAxisLinesAction();
				break;
			case KeyEvent.VK_W:
				System.out.println("W key pressed");
				fwdBwdAction((float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_S:
				System.out.println("S key pressed");
				fwdBwdAction(-(float)elapsedTimeOffset);	
				break;
			case KeyEvent.VK_A:
				System.out.println("A key pressed");
				strifeAction(-(float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_D:
				System.out.println("D key pressed");
				strifeAction((float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_E:
				System.out.println("E key pressed");
				upDownAction(-(float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_Q:
				System.out.println("Q key pressed");
				upDownAction((float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_L:
				System.out.println("L pressed.");
				toggleLightAction();
				break;
			case KeyEvent.VK_LEFT:
				System.out.println("Arrow left key pressed");
				panAction((float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_RIGHT:
				System.out.println("Arrow right key pressed");
				panAction(-(float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_UP:
				System.out.println("Arrow up key pressed");
				pitchAction((float)elapsedTimeOffset);
				break;
			case KeyEvent.VK_DOWN:
				System.out.println("Arrow down key pressed");
				pitchAction(-(float)elapsedTimeOffset);
				break;
			default:
				System.out.println("No command found.");
		}
	}

	/** override if desired. */
	@Override
	public void keyReleased(KeyEvent e) {}

	/** override if desired. */
	@Override
	public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
        float zoomFactor = 1.0f + (0.1f * rotation);
		
		if(zoomFactor < 1.0f) { renderer.addCurrentLightPositionY(zoomFactor); }  // moves light up
		if(zoomFactor > 1.0f) { renderer.addCurrentLightPositionY(-zoomFactor); }  // moves light down
    }


	@Override
	public void mouseDragged(MouseEvent e) {
		System.out.println("Mouse being dragged..");
		if (lastVarsSet == 0) {
			lastX = e.getX();
			lastY = e.getY();
			lastVarsSet = 1;
		}
        int x = e.getX();
        int y = e.getY();

        // Calculate the mouse movement
        int dx = x - lastX;
        int dy = y - lastY;

		renderer.lightPositionAdd(dx*0.1f, 0, dy*-0.1f);
        // Remember the current mouse position
        lastX = x;
        lastY = y;
    }

	@Override
    public void mouseMoved(MouseEvent e) {} // Not used in this example

	// **************** Key Listener Actions ****************************
	private void shutdownAction() { System.out.println("Shutting down."); System.exit(0); }

	/** toggle the VISIBILITY of the axis lines */
	public void toggleAxisLinesAction() { displayAxisLines = displayAxisLines ? false : true; }
	
	/** Move the camera forward and backward */
	public void fwdBwdAction(float newelapsedTimeOffset){ camera.forward(newelapsedTimeOffset); }

	/** Move the camera parallel to the V vetor up in positive and negative values (scale V vector */
	public void upDownAction(float newelapsedTimeOffset) { camera.up(newelapsedTimeOffset); }

	/** Strife the camera (scale the U vector)  */
	public void strifeAction(float newelapsedTimeOffset){ camera.strife(newelapsedTimeOffset); }

	/** pan the camera (routate about the Y axis) */
	public void panAction(float newelapsedTimeOffset) { camera.pan(newelapsedTimeOffset); }

	/** Pitch the camera up down (rotate about the U Vector) */ 
	public void pitchAction(float newelapsedTimeOffset) { camera.pitch(newelapsedTimeOffset); }

	public void toggleLightAction() { renderer.toggleLight(); }

	// **************** Main ****************************
	public static void main(String[] args) { new Code(); }
}