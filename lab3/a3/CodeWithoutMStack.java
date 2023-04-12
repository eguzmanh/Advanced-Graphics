package a3;

import a3.shapes.*;

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
import com.jogamp.common.nio.Buffers;
import org.joml.*;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class Code extends JFrame implements GLEventListener, KeyListener {	
	private GLCanvas myCanvas;
	private Camera camera;
	private Renderer renderer;

	private int numObjects;

	private int brickTexture, iceTexture, artsyTexture, dolTexture, floralSheetTexture, drawerDoorTexture, marsDiffuseTexture;
	// private int numDolVertices;

	private double lastFrameTime, currFrameTime, elapsedTime;

	private float speed;
	private float aspect;
	
	private boolean displayAxisLines;

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat;  // perspective matrix
	private Matrix4f vMat;  // view matrix
	private Matrix4f mMat;  // model matrix
	private Matrix4f mvMat; // model-view matrix
	// private Matrix4fStack mvStack;

	private Pyramid brickPyramid;
	private Pyramid icePyramid;
	private Cube artsyCube;
	private Mars mars;
	private Dolphin dol;

	private ImportedModel marsObj, dolphinObj;
	// private ImportedModel dolphin;


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
		
		setPerspective();
		loadObjects();
		loadTextures();
		buildWorldObjects();
		
		renderer.bindVertextAttributeData();
		
		bindWorldObjects();
	}
	
	private void styleJFrame() {
		setTitle("CSC 155 - LAB 2");
		setSize(800, 800);
	}

	private void initMainComponents() {
		camera = new Camera();
	}

	private void initMatrices() {
		// Matrices for the model view stack
		pMat = new Matrix4f(); 
		vMat = new Matrix4f();  
		mMat = new Matrix4f();  
		mvMat = new Matrix4f();
		// mvStack = new Matrix4fStack(7);
	}

	private void initCanvas() {
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);

		this.add(myCanvas);
		this.setVisible(true);
	}

	private void initAnimator() {
		Animator animator = new Animator(myCanvas);
		animator.start();
	}
	
	private void initTimeFrames() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsedTime = 0.0;
	}
	
	private void setPerspective() {
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}

	private void loadObjects() {
		marsObj = new ImportedModel("assets/models/mars.obj");
		dolphinObj = new ImportedModel("assets/models/dolphinHighPoly.obj");
	}

	// refer to readme for source explanations
	private void loadTextures() {
		brickTexture = Utils.loadTexture("assets/textures/brick1.jpg");
		iceTexture = Utils.loadTexture("assets/textures/ice.jpg");
		dolTexture =  Utils.loadTexture("assets/textures/Dolphin_HighPolyUV.png");
		artsyTexture = Utils.loadTexture("assets/textures/pexels-anni-roenkae.jpg");
		floralSheetTexture = Utils.loadTexture("assets/textures/floral_sheet.png");
		drawerDoorTexture = Utils.loadTexture("assets/textures/Drawer_Door.jpg");
		marsDiffuseTexture = Utils.loadTexture("assets/textures/mars/Diffuse_2K.png");
	}

	private void buildWorldObjects() {	
		brickPyramid = new Pyramid("pyramid");
		icePyramid = new Pyramid("pyramid");
		artsyCube = new Cube("cube");
		mars = new Mars("sphere", marsObj);
		dol = new Dolphin("dolphin", dolphinObj);
	}

	private void bindWorldObjects() {
		renderer.bindTexturedWorldObject(brickPyramid, brickPyramid.getVertices(), brickPyramid.getBrickTexCoords());
		renderer.bindTexturedWorldObject(icePyramid, icePyramid.getVertices(), icePyramid.getIceTexCoords());
		renderer.bindTexturedWorldObject(artsyCube, artsyCube.getVertices(), artsyCube.getArtsyTextureCoordinates());
		renderer.bindTexturedWorldObject(mars, mars.getVertices(), mars.getTextureCoordinates());
		renderer.bindTexturedWorldObject(dol, dol.getVertices(), dol.getTextureCoordinates());
	}


	// ************************* Runtime Actions *************************
	/** Renders on every frame and does actions */
	public void display(GLAutoDrawable drawable) {
		renderer.clearGL();
		renderer.useMainShader();

		upateElapsedTimeInfo();
		setPerspective();

		// * View Matrix from Camera
		vMat = camera.getViewMatrix();
	
		renderWorldObjects();		
	}

	private void renderWorldObjects() {

		// * brick Pyramid Object
		updateBrickPyramid();

		// * Ice Pyramid Object
		updateIcePyramid();
		
		// * Artsy Cube Object
		updateArtsyCube();

		// * Mars Object
		updateMars();

		// * Dolphin Object
		updateDolphin();

		renderAxisLines();
		
	}

	/** Multiply the View and Model matrices to the Model-View identity matrix */
	private void updateMVMatrix() {
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		renderer.setMPUniformVars(mvMat, pMat);	
	}

	private void updateBrickPyramid() {
		brickPyramid.update(speed); // pyramid 1

		Vector3f brickPyramidLocation = brickPyramid.getLocation();
		mMat.translation(brickPyramidLocation.x()-2.5f, brickPyramidLocation.y(), brickPyramidLocation.z());
		mMat.rotate(brickPyramid.getRotAmnt(), 0.0f, 1.0f, 0.0f);

		updateMVMatrix();
	
		renderer.renderWorldObject(brickPyramid.getVBOIndex(), brickPyramid.getNumVertices(), brickPyramid.getVBOTxIndex(), brickTexture);
	}

	private void updateIcePyramid() {
		icePyramid.update(speed); // pyramid 2

		Vector3f icePyramidLocation = icePyramid.getLocation();
		mMat.translation(icePyramidLocation.x()+2.5f, icePyramidLocation.y(), icePyramidLocation.z());
		mMat.rotate(icePyramid.getRotAmnt(), 0.0f, -1.0f, 0.0f);

		updateMVMatrix();
	
		renderer.renderWorldObject(icePyramid.getVBOIndex(), icePyramid.getNumVertices(), icePyramid.getVBOTxIndex(), iceTexture);
	}

	private void updateArtsyCube() {
		artsyCube.update(speed); // pyramid 2

		Vector3f artsyCubeLoc = artsyCube.getLocation();
		mMat.translation(artsyCubeLoc.x()+1f, artsyCubeLoc.y() + artsyCube.getTranslationOffset(), artsyCubeLoc.z());	
		mMat.rotate(artsyCube.getRotAmnt(), 0.0f, 1.0f, 0.0f);

		updateMVMatrix();

		renderer.renderWorldObject(artsyCube.getVBOIndex(), artsyCube.getNumVertices(), artsyCube.getVBOTxIndex(), artsyTexture);
	}

	private void updateMars() {
		mars.update(speed); // pyramid 2

		Vector3f marsLoc = mars.getLocation();
		mMat.translation(marsLoc.x(), marsLoc.y(), marsLoc.z());	
		// mMat.rotate(mars.getRotAmnt(), 0.0f, 1.0f, 0.0f);
		mMat.scale(0.5f, 0.5f, 0.5f);	

		updateMVMatrix();

		renderer.renderWorldObject(mars.getVBOIndex(), mars.getNumVertices(), mars.getVBOTxIndex(), marsDiffuseTexture);
	}

	private void updateDolphin() {
		dol.update(speed);

		Vector3f dolLoc = dol.getLocation();
		mMat.translation(dolLoc.x(), dolLoc.y(), dolLoc.z() + dol.getZOffset());
		if (dol.getZDirection() < 0) mMat.rotate(135, 0.0f, 1.0f, 0.0f);
		
		mMat.scale(1.5f, 1.5f, 1.5f);

		updateMVMatrix();

		renderer.renderWorldObject(dol.getVBOIndex(), dol.getNumVertices(), dol.getVBOTxIndex(), dolTexture);
	}

	private void renderAxisLines() {
		if (displayAxisLines) {
			renderer.renderAxisLines(vMat, pMat);
		}
	}
	
	// Deals with elapsed time and ensure that the values stay close
	private void upateElapsedTimeInfo() {
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();

		// if (currFrameTime - lastFrameTime > 15.0) { elapsedTime %= 15.0; }
		// else { elapsedTime = (currFrameTime - lastFrameTime); }


		if (currFrameTime - lastFrameTime > 100.0) { elapsedTime %= 15.0; }
		else { elapsedTime = (currFrameTime - lastFrameTime); }
		
		elapsedTime = (currFrameTime - lastFrameTime);
		speed = (float)elapsedTime / 500f;
	}


	// ************************* Overrides *************************
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
				fwdBwdAction((float)speed);
				break;
			case KeyEvent.VK_S:
				System.out.println("S key pressed");
				fwdBwdAction(-(float)speed);	
				break;
			case KeyEvent.VK_A:
				System.out.println("A key pressed");
				strifeAction(-(float)speed);
				break;
			case KeyEvent.VK_D:
				System.out.println("D key pressed");
				strifeAction((float)speed);
				break;
			case KeyEvent.VK_E:
				System.out.println("E key pressed");
				upDownAction(-(float)speed);
				break;
			case KeyEvent.VK_Q:
				System.out.println("Q key pressed");
				upDownAction((float)speed);
				break;
			case KeyEvent.VK_LEFT:
				System.out.println("Arrow left key pressed");
				panAction((float)speed);
				break;
			case KeyEvent.VK_RIGHT:
				System.out.println("Arrow right key pressed");
				panAction(-(float)speed);
				break;
			case KeyEvent.VK_UP:
				System.out.println("Arrow up key pressed");
				pitchAction((float)speed);
				break;
			case KeyEvent.VK_DOWN:
				System.out.println("Arrow down key pressed");
				pitchAction(-(float)speed);
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
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}


	// **************** Key Listener Actions ****************************
	private void shutdownAction() { System.out.println("Shutting down."); System.exit(0); }

	/** toggle the VISIBILITY of the axis lines */
	public void toggleAxisLinesAction() { displayAxisLines = displayAxisLines ? false : true; }
	
	/** Move the camera forward and backward */
	public void fwdBwdAction(float newSpeed){ camera.forward(newSpeed); }

	/** Move the camera parallel to the V vetor up in positive and negative values (scale V vector */
	public void upDownAction(float newSpeed) { camera.up(newSpeed); }

	/** Strife the camera (scale the U vector)  */
	public void strifeAction(float newSpeed){ camera.strife(newSpeed); }

	/** pan the camera (routate about the Y axis) */
	public void panAction(float newSpeed) { camera.pan(newSpeed); }

	/** Pitch the camera up down (rotate about the U Vector) */ 
	public void pitchAction(float newSpeed) { camera.pitch(newSpeed); }


	// **************** Main ****************************
	public static void main(String[] args) { new Code(); }
}