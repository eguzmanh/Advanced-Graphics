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

	// private int numDolVertices;

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
	private Matrix4f mvMat; // model-view matrix
	private Matrix4fStack mStack;


	private int brickTexture, iceTexture, artsyTexture, dolTexture, floralSheetTexture, 
				drawerDoorTexture, marsDiffuseTexture, skyboxTexture;
	
	private Pyramid brickPyramid;
	private Pyramid icePyramid;
	private Cube artsyCube;
	private Cube skyboxCube;
	private Mars mars;
	private Dolphin dol;

	private ImportedModel marsObj, dolphinObj, pyramidObj;

	private Vector3f currObjLoc;
	private float x,y,z;
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
		mvMat = new Matrix4f();
		invTrMat = new Matrix4f(); 
		mStack = new Matrix4fStack(7);
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
		startTime = System.currentTimeMillis();
		elapsedTime = 0.0;
	}
	
	private void setPerspective() {
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}

	private void loadObjects() {
		marsObj = new ImportedModel("assets/models/mars.obj");
		dolphinObj = new ImportedModel("assets/models/dolphinHighPoly.obj");
		pyramidObj = new ImportedModel("assets/models/pyr.obj");
	}

	// refer to readme for source explanations
	private void loadTextures() {
		dolTexture =  Utils.loadTexture("assets/textures/Dolphin_HighPolyUV.png");
		artsyTexture = Utils.loadTexture("assets/textures/pexels-anni-roenkae.jpg");
		marsDiffuseTexture = Utils.loadTexture("assets/textures/mars/Diffuse_2K.png");
		brickTexture = Utils.loadTexture("assets/textures/brick1.jpg");

		skyboxTexture = Utils.loadCubeMap("assets/cubeMaps/LakeIslands");
		renderer.enableCubeMap();

		// ! Unused
		iceTexture = Utils.loadTexture("assets/textures/ice.jpg");
		floralSheetTexture = Utils.loadTexture("assets/textures/floral_sheet.png");
		drawerDoorTexture = Utils.loadTexture("assets/textures/Drawer_Door.jpg");
	}

	private void buildWorldObjects() {	
		skyboxCube = new Cube("cube");
		brickPyramid = new Pyramid("pyramid", pyramidObj);
		icePyramid = new Pyramid("pyramid", pyramidObj);
		artsyCube = new Cube("cube");
		mars = new Mars("sphere", marsObj);
		dol = new Dolphin("dolphin", dolphinObj);
	}

	private void bindWorldObjects() {
		renderer.bindWorldObject(skyboxCube, skyboxCube.getVertices());
		renderer.bindTexturedWorldObject(dol, dol.getVertices(), dol.getTextureCoordinates(), dol.getNormals());
		renderer.bindTexturedWorldObject(artsyCube, artsyCube.getVertices(), artsyCube.getArtsyTextureCoordinates(), artsyCube.getNormals());
		renderer.bindTexturedWorldObject(mars, mars.getVertices(), mars.getTextureCoordinates(), mars.getNormals());
		renderer.bindTexturedWorldObject(brickPyramid, brickPyramid.getVertices(), brickPyramid.getTextureCoordinates(), brickPyramid.getNormals());
		renderer.bindTexturedWorldObject(icePyramid, icePyramid.getVertices(), icePyramid.getTextureCoordinates(), icePyramid.getNormals());
	}


	// ************************* Runtime Actions *************************
	/** Renders on every frame and does actions */
	public void display(GLAutoDrawable drawable) {
		renderer.clearGL();

		upateElapsedTimeInfo();
		setPerspective();
		vMat.set(camera.getViewMatrix());
		
		renderer.useCubeMapShader();
		renderSkybox();

		// * View Matrix from Camera
		renderer.useMainShader();
		renderWorldObjects();		


		renderer.useLineShader();
		renderAxisLines();
	}

	private void renderSkybox() {
		renderer.setVPUniformVar(vMat, pMat);
		renderer.drawCubeMap(skyboxCube.getVBOIndex(), skyboxCube.getNumVertices(), skyboxTexture);
	}

	private void renderWorldObjects() {
		// ***** Using mvMatrix *****

		// * Dolphin Object
		updateDolphin();
		
		mMat.identity();
		// * Artsy Cube Object
		updateArtsyCube();

		updateObjectMatrixStack();
		

		// ***** Using different shaderPrograms *****
		mMat.identity();
	}

	/** Multiply the View and Model matrices to the Model-View identity matrix */
	private void updateMMatrix() {
		// mMat.identity();
		// mMat.mul(vMat);
		// mMat.mul(mMat);
		// amt += elapsedTime * 0.03f;
		renderer.setupLights(elapsedTimeOffset);
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		renderer.setMVPUniformVars(mMat, vMat, pMat, invTrMat);
	}
	
	private void updateObjectMatrixStack() {
		// ********************** Using mMatrixStack **********************
		// mStack.pushMatrix();
		// mStack.set(vMat);
		// mStack.translate(camera.getLocation().x(),camera.getLocation().y(),camera.getLocation().z());


		// * Mars Object -- parent
		mStack.pushMatrix();

		// operate on the mars object
		// mStack.pushMatrix();
		updateMars();
		// mStack.popMatrix();

		// * Ice Pyramid Object   -- child of mars
		mStack.pushMatrix();
		updateIcePyramid();
		mStack.popMatrix();

		// * brick Pyramid Object -- child of mars
		// no need to keep a parent since the pyramids are children of Mars
		mStack.pushMatrix();
		updateBrickPyramid();
		mStack.popMatrix();
		

		mStack.popMatrix(); // pop mars mv
		// mStack.popMatrix(); // pop camera viewT
	}

	private void updateDolphin() {
		dol.update(elapsedTimeOffset);
		currObjLoc = dol.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z() + elapsedTimeOffset * dol.getZDirection();

		dol.setLocation(x, y, z);
		
		mMat.translation(x, y, z);
		mMat.scale(1.5f, 1.5f, 1.5f);
		
		if (dol.getZDirection() < 0) mMat.rotate(135, 0.0f, 1.0f, 0.0f);

		updateMMatrix();

		renderer.drawWorldObject(dol.getVBOIndex(), dol.getNumVertices(), dol.getVBOTxIndex(), dolTexture, dol.getVBONIndex());
	}

	private void updateArtsyCube() {
		artsyCube.update(elapsedTimeOffset, 0.0f, 5.0f); // pyramid 2

		currObjLoc = artsyCube.getLocation();
		x = currObjLoc.x();
		y = currObjLoc.y() + elapsedTimeOffset * artsyCube.getYDir();
		z = currObjLoc.z();
		artsyCube.setLocation(x,y,z);

		mMat.translation(x, y, z);	
		mMat.rotate(artsyCube.getRotationAngle(), 0.0f, 1.0f, 0.0f);

		updateMMatrix();

		renderer.drawWorldObject(artsyCube.getVBOIndex(), artsyCube.getNumVertices(), artsyCube.getVBOTxIndex(), artsyTexture, artsyCube.getVBONIndex());
	}

	private void updateMars() {
		mars.update(elapsedTimeOffset);
		currObjLoc = mars.getLocation();

		// x = currObjLoc.x() + elapsedTimeOffset * mars.getXDirection();
		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();

		// mars.setLocation(x, y, z);

		mStack.translate(x,y,z);	
		mStack.scale(0.5f, 0.5f, 0.5f);	
		
		renderer.setupLights(elapsedTimeOffset);
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		renderer.setMVStackUniformVar(vMat, mStack, invTrMat);

		renderer.drawWorldObject(mars.getVBOIndex(), mars.getNumVertices(), mars.getVBOTxIndex(), marsDiffuseTexture, mars.getVBONIndex());
	}

	private void updateIcePyramid() {
		icePyramid.update(elapsedTimeOffset); // pyramid 1
		currObjLoc = icePyramid.getLocation();

		x = currObjLoc.x() + elapsedTimeOffset;
		y = currObjLoc.y();
		z = currObjLoc.z() + elapsedTimeOffset;

		icePyramid.setLocation(x,y,z);
		currObjLoc = icePyramid.getLocation();
		
		mStack.translate(-(float)Math.sin(x)*5.0f,y,(float)Math.cos(z)*5.0f);
		mStack.rotate(icePyramid.getRotationAngle(), 0.0f, 1.0f, 0.0f);
		
		renderer.setupLights(elapsedTimeOffset);
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		renderer.setMVStackUniformVar(vMat, mStack, invTrMat);

		renderer.drawWorldObject(icePyramid.getVBOIndex(), icePyramid.getNumVertices(), icePyramid.getVBONIndex());
	}

	private void updateBrickPyramid() {
		brickPyramid.update(elapsedTimeOffset); // pyramid 2
		currObjLoc = brickPyramid.getLocation();

		x = currObjLoc.x() + elapsedTimeOffset;
		y = currObjLoc.y();
		z = currObjLoc.z() + elapsedTimeOffset;
		
		brickPyramid.setLocation(x,y,z);
		currObjLoc = brickPyramid.getLocation();

		mStack.translate((float)Math.sin(x)*5.0f,y,(float)Math.cos(z)*5.0f);
		mStack.rotate(brickPyramid.getRotationAngle(), 0.0f, -1.0f, 0.0f);

		renderer.setupLights(elapsedTimeOffset);
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		renderer.setMVStackUniformVar(vMat, mStack, invTrMat);

		renderer.drawWorldObject(brickPyramid.getVBOIndex(), brickPyramid.getNumVertices(), brickPyramid.getVBONIndex());

	}

	private void renderAxisLines() {
		if (displayAxisLines) {
			renderer.renderAxisLines(vMat, mMat, pMat);
		}
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


	// **************** Main ****************************
	public static void main(String[] args) { new Code(); }
}