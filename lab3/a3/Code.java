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
	private Matrix4fStack mStack;


	private int brickTexture, iceTexture, artsyTexture, dolTexture, floralSheetTexture, 
				drawerDoorTexture, marsDiffuseTexture, skyboxTexture, boatTexture, waterTankTexture, fishTexture;
	
	private Pyramid brickPyramid;
	private Pyramid icePyramid;
	private Cube skyboxCube;
	private Cube waterTankBox1;
	private Cube waterTankBox2;
	private Mars mars;
	private Dolphin dol;
	private Boat boat;
	private WaterTank waterTank;
	private Fish fish;

	private ImportedModel marsObj, dolphinObj, pyramidObj, boatObj, waterTankObj, fishObj;

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
		invTrMat = new Matrix4f(); 
		mStack = new Matrix4fStack(7);
	}

	private void initCanvas() {
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);
		myCanvas.addMouseWheelListener(this);
		myCanvas.addMouseMotionListener(this);
		// myCanvas.addMouseMotionListener(new MouseHandler());
		// myCanvas.addMouseListener(new MouseHandler());
        // myCanvas.addMouseMotionListener(new MouseHandler());
		// getContentPane()addMouseListener(new MouseHandler());
		// getContentPane().getComponent(0).addMouseMotionListener(new MouseHandler());
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
	}

	private void loadObjects() {
		// marsObj = new ImportedModel("assets/models/mars.obj");
		dolphinObj = new ImportedModel("assets/models/dolphinHighPoly.obj");
		// pyramidObj = new ImportedModel("assets/models/pyr.obj");
		boatObj = new ImportedModel("assets/models/MarlowBoat.obj");
		waterTankObj = new ImportedModel("assets/models/waterTank.obj");
		fishObj = new ImportedModel("assets/models/fish.obj");
	}

	// refer to readme for source explanations
	private void loadTextures() {
		dolTexture =  Utils.loadTexture("assets/textures/Dolphin_HighPolyUV.png");
		fishTexture = Utils.loadTexture("assets/textures/fish_texture.png");


		// artsyTexture = Utils.loadTexture("assets/textures/pexels-anni-roenkae.jpg");
		// marsDiffuseTexture = Utils.loadTexture("assets/textures/mars/Diffuse_2K.png");
		brickTexture = Utils.loadTexture("assets/textures/brick1.jpg");

		skyboxTexture = Utils.loadCubeMap("assets/cubeMaps/LakeIslands");
		renderer.enableCubeMap();
		
		boatTexture = Utils.loadTexture("assets/textures/boat/HullTexture.png");
		waterTankTexture = Utils.loadTexture("assets/textures/water-tank/colour.png");

		// ! Unused
		// iceTexture = Utils.loadTexture("assets/textures/ice.jpg");
		// floralSheetTexture = Utils.loadTexture("assets/textures/floral_sheet.png");
		// drawerDoorTexture = Utils.loadTexture("assets/textures/Drawer_Door.jpg");
	}

	private void buildWorldObjects() {	
		skyboxCube = new Cube("cube");
		// brickPyramid = new Pyramid("pyramid", pyramidObj);
		// icePyramid = new Pyramid("pyramid", pyramidObj);
		waterTankBox1 = new Cube("cube");
		waterTankBox2 = new Cube("cube");
		// mars = new Mars("sphere", marsObj);
		dol = new Dolphin("dolphin", dolphinObj);

		boat = new Boat("boat", boatObj);
		waterTank = new WaterTank("waterTank", waterTankObj);

		fish = new Fish("fish", fishObj);
	}

	private void bindWorldObjects() {
		renderer.bindWorldObject(skyboxCube, skyboxCube.getVertices());
		renderer.bindTexturedWorldObject(dol, dol.getVertices(), dol.getTextureCoordinates(), dol.getNormals());
		renderer.bindTexturedWorldObject(boat, boat.getVertices(), boat.getTextureCoordinates(), boat.getNormals());
		renderer.bindTexturedWorldObject(fish, fish.getVertices(), fish.getTextureCoordinates(), fish.getNormals());

		renderer.bindTexturedWorldObject(waterTank, waterTank.getVertices(), waterTank.getTextureCoordinates(), waterTank.getNormals());
		renderer.bindWorldObjectWNormals(waterTankBox1, waterTankBox1.getVertices(), waterTankBox1.getNormals());
		renderer.bindWorldObjectWNormals(waterTankBox2, waterTankBox2.getVertices(), waterTankBox2.getNormals());
		// renderer.bindTexturedWorldObject(mars, mars.getVertices(), mars.getTextureCoordinates(), mars.getNormals());
		// renderer.bindTexturedWorldObject(brickPyramid, brickPyramid.getVertices(), brickPyramid.getTextureCoordinates(), brickPyramid.getNormals());
		// renderer.bindTexturedWorldObject(icePyramid, icePyramid.getVertices(), icePyramid.getTextureCoordinates(), icePyramid.getNormals());
	}


	// ************************* Runtime Actions *************************
	/** Renders on every frame and does actions */
	public void display(GLAutoDrawable drawable) {
		renderer.clearGL();
		
		upateElapsedTimeInfo();
		setPerspective();
		vMat.set(camera.getViewMatrix());

		System.out.println(renderer.lightStatus());
		
		renderer.useCubeMapShader();
		renderSkybox();
		
		renderer.useLineShader();
		renderAxisLines();

		renderer.useLightDotShader();
		renderLightDot();

		// * View Matrix from Camera
		renderer.useMainShader();
		renderer.setLightStatus();
		renderer.setupLights(elapsedTimeOffset);
		renderWorldObjects();		
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
	

	private void renderWorldObjects() {

		// * Dolphin Object
		mMat.identity();
		updateDolphin();
		mMat.identity();
		updateWaterTank();
		mMat.identity();
		updateWaterTankBox1();
		mMat.identity();
		updateWaterTankBox2();

		// mMat.identity();
		// updateBoat();

		
		// mMat.identity();
		// * Artsy Cube Object
		// updateArtsyCube();

		updateObjectMatrixStack();
		

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

	private void updateMStackMatrix() {
		renderer.setupLights(elapsedTimeOffset);
		mStack.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		// renderer.setMVPUniformVars(mStack, vMat, pMat, invTrMat);
		renderer.setMVStackUniformVar(vMat, mStack, invTrMat);
	}
	
	private void updateObjectMatrixStack() {
		// ********************** Using mMatrixStack **********************
		// mStack.pushMatrix();
		// mStack.set(vMat);
		// mStack.translate(camera.getLocation().x(),camera.getLocation().y(),camera.getLocation().z());

		// * Mars Object -- parent
		mStack.pushMatrix();

		mStack.pushMatrix();
		updateBoat();
		// mStack.popMatrix();

		// operate on the mars object
		// mStack.pushMatrix();
		// updateMars();
		// mStack.popMatrix();

		// * Ice Pyramid Object   -- child of mars
		mStack.pushMatrix();
		// updateIcePyramid();
		updateFish();
		mStack.popMatrix();

		// * brick Pyramid Object -- child of mars
		// no need to keep a parent since the pyramids are children of Mars
		// mStack.pushMatrix();
		// updateBrickPyramid();
		// mStack.popMatrix();
		
		mStack.popMatrix();
		mStack.popMatrix(); // pop mars mv
		// mStack.popMatrix(); // pop camera viewT
	}

	private void updateDolphin() {
		dol.update(elapsedTimeOffset);
		
		
		currObjLoc = dol.getLocation();

		// if (currObjLoc.x >= 15f) { mMat.rotate(135, 0.0f, 1.0f, 0.0f); }
		
		x = currObjLoc.x() + elapsedTimeOffset * dol.getXDirection();
		y = currObjLoc.y();
		// z = currObjLoc.z() + elapsedTimeOffset * dol.getZDirection();
		z = currObjLoc.z();
		
		dol.setLocation(x, y, z);
		
		mMat.translation(x, y, z);
		mMat.scale(2.0f, 2.0f, 2.0f);

		if (dol.getXDirection() < 0) { mMat.rotate(-1.0f, 0.0f, 1.0f, 0.0f); }
		else {mMat.rotate(2f, 0.0f, 1.0f, 0.0f);}

		updateMMatrix();
		
		renderer.renderWorldObject(dol.getVBOIndex(), dol.getNumVertices(), dol.getVBOTxIndex(), dolTexture, dol.getVBONIndex());
	}

	private void updateBoat() {
		boat.update(elapsedTimeOffset);
		currObjLoc = boat.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z() + elapsedTimeOffset * boat.getZDirection();

		boat.setLocation(x, y, z);

		mStack.translation(x, y, z);
		mStack.scale(0.4f, 0.4f, 0.4f);

		if(boat.getZDirection() < 0 ) mStack.rotate(-3.2f, 0f, 1f, 0f);

		updateMStackMatrix();

		renderer.renderWorldObject(boat.getVBOIndex(), boat.getNumVertices(), boat.getVBOTxIndex(), boatTexture, boat.getVBONIndex());
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
		mMat.scale(0.6f, 0.6f, 0.5f);
		mMat.rotate(1.5f, 0f,1f,0f);
		
		// if (boat.getZDirection() < 0) mMat.rotate(135, 0.0f, 1.0f, 0.0f);
		// renderer.setNeutralWhiteMaterial();
		// renderer.setupLights(elapsedTimeOffset);
		updateMMatrix();

		renderer.renderWorldObject(waterTank.getVBOIndex(), waterTank.getNumVertices(), waterTank.getVBOTxIndex(), waterTankTexture, waterTank.getVBONIndex());
	}

	private void updateWaterTankBox1() {
		waterTankBox1.update(elapsedTimeOffset, 0.0f, 5.0f); // pyramid 2

		currObjLoc = waterTankBox1.getLocation();
		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();

		waterTankBox1.setLocation(x,y,z);

		mMat.translation(x+1.5f, y, z);	
		// mMat.rotate(waterTankBox1.getRotationAngle(), 0.0f, 1.0f, 0.0f);
		mMat.scale(0.5f, 0.5f, 0.5f);
		
		renderer.setGoldMaterial();
		renderer.setupLights(elapsedTimeOffset);
		updateMMatrix();

		renderer.renderWorldObject(waterTankBox1.getVBOIndex(), waterTankBox1.getNumVertices(), waterTankBox1.getVBONIndex());
	}

	private void updateWaterTankBox2() {
		waterTankBox2.update(elapsedTimeOffset, 0.0f, 5.0f); // pyramid 2

		currObjLoc = waterTankBox2.getLocation();
		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();

		waterTankBox2.setLocation(x,y,z);

		mMat.translation(x-1.5f, y, z);	
		// mMat.rotate(waterTankBox2.getRotationAngle(), 0.0f, 1.0f, 0.0f);
		mMat.scale(0.5f, 0.5f, 0.5f);
		
		renderer.setAmethystMaterial();
		renderer.setupLights(elapsedTimeOffset);
		updateMMatrix();

		renderer.renderWorldObject(waterTankBox2.getVBOIndex(), waterTankBox2.getNumVertices(), waterTankBox2.getVBONIndex());
	}


	private void updateFish() {
		fish.update(elapsedTimeOffset); // pyramid 1
		currObjLoc = fish.getLocation();

		// x = currObjLoc.x() + elapsedTimeOffset;
		x = currObjLoc.x;
		y = currObjLoc.y;
		// z = currObjLoc.z() + elapsedTimeOffset;
		z = currObjLoc.z;

		fish.setLocation(x,y,z);
		// currObjLoc = fish.getLocation();
		
		mStack.translate(1f,4f, -7f);
		// mStack.translate(x*5f,y,z);
		mStack.rotate(0.7f, 0.0f, 0.0f, 1.0f);

		// renderer.setAmethystMaterial();
		// renderer.setupLights(elapsedTimeOffset);

		mStack.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		renderer.setMVStackUniformVar(vMat, mStack, invTrMat);

		renderer.renderWorldObject(fish.getVBOIndex(), fish.getNumVertices(), fish.getVBOTxIndex(), fishTexture, fish.getVBONIndex());
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

	private int lastX, lastY, lastVarsSet = 0;

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