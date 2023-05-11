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

	
	// private ImportedModel dolphinObj, waterTankObj;
	private ImportedModel chessBoardObj, chessKingObj, chessQueenObj, chessBishopObj, chessKnightObj, chessRookObj, chessPawnObj;
	private ImportedModel groundObj;
	

	private float terLocX, terLocY, terLocZ;
	private int rockyTexture;
	private int heightMap;

	private Ground ground;

	private Cube skyboxCube;
	// private Dolphin dol;
	// private WaterTank water Tank;
	// private Cube waterTankBox1;
	private ChessBoard chessBoard;

	// ChessPieces for each side
	// private ChessPiece chessKing1, chessQueen1, chessBishop1, chessKnight1, chessRook1, chessPawn1;
	private ChessPiece chessKingWhite, chessKingBlack, chessRookWhite1, chessRookWhite2, chessQueenBlack, chessQueenWhite;

	private SpaceSphere geoSphere;
	private ChessPiece chessKingBlackGeom;
	// private ChessPiece chessKing2, chessQueen2, chessBishop2, chessKnight2, chessRook2, chessPawn2;
	
	// private int dolTexture, skyboxTexture, waterTankTexture;
	private int skyboxTexture, chessBoardTexture;

	private Vector3f currObjLoc;
	private float x,y,z;

	private int lastX, lastY, lastVarsSet = 0;


	
	// private int squareMoonTexture;
	// private int squareMoonHeight;
	// private int squareMoonNormalMap;


	// VR stuff
	// chnange the IOD value to enhance the distance between the red and cyan output of the renderer
	// 2.5 seeems to work with this program ti render the scene in the red/cyan glasses
	private float IOD = 2.5f;  // tunable interocular distance � we arrived at 0.01 for this scene by trial-and-error
	private float near = 0.01f;
	private float far = 100.0f;
	private int sizeX = 1920, sizeY = 1080;
	
	private boolean use3DAnaglyphs;

	// **************** Constructor(s) ****************************
	public Code() {	
		numObjects = 13;
		displayAxisLines = true;
		use3DAnaglyphs = false;
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
		// ightVmat.identity().setLookAt(renderer.getCurrentLight(), camera.getLocation(), camera.getV());	// vector from light to origin
		lightPmat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}

	private void computePerspectiveMatrix(float leftRight)
	{	float top = (float)Math.tan(1.0472f / 2.0f) * (float)near;
		float bottom = -top;
		float frustumshift = (IOD / 2.0f) * near / far;
		float left = -aspect * top - frustumshift * leftRight;
		float right = aspect * top - frustumshift * leftRight;
		pMat.setFrustum(left, right, bottom, top, near, far);
	}

	private void loadObjects() {
		chessBoardObj = new ImportedModel("assets/models/chess/Board_HalfSize2.obj");
		
		// Chess Pieces
		chessKingObj = new ImportedModel("assets/models/chess/King.obj");
		chessRookObj = new ImportedModel("assets/models/chess/Rook.obj");
		
		chessQueenObj = new ImportedModel("assets/models/chess/Queen2.obj");

		
		chessBishopObj = new ImportedModel("assets/models/chess/Bishop2.obj");
		chessKnightObj = new ImportedModel("assets/models/chess/Knight2.obj");
		chessPawnObj = new ImportedModel("assets/models/chess/Pawn.obj");


		// two Kings, two rooks
		// checkmate scene
	}

	// refer to readme for source explanations
	private void loadTextures() {
		skyboxTexture = Utils.loadCubeMap("assets/cubeMaps/space/red");
		chessBoardTexture = Utils.loadTexture("assets/textures/chess/WoodenChessBoard_diffuse.jpg");
		renderer.init3DMarbleTexture();
	}

	private void buildWorldObjects() {	
		skyboxCube = new Cube("cube");

		geoSphere = new SpaceSphere("sphere");
		geoSphere.setLocation(0f, 5f, 0f);

		chessKingBlackGeom = new ChessPiece("chessPiece", chessKingObj);

		chessBoard = new ChessBoard("chessBoard", chessBoardObj);

		chessKingBlack = new ChessPiece("chessPiece", chessKingObj);
		chessKingWhite = new ChessPiece("chessPiece", chessKingObj);

		chessQueenBlack = new ChessPiece("chessPiece", chessQueenObj);
		chessQueenWhite = new ChessPiece("chessPiece", chessQueenObj);
		
		chessRookWhite1 = new ChessPiece("chessPiece", chessRookObj);
		chessRookWhite2 = new ChessPiece("chessPiece", chessRookObj);
	}

	private void bindWorldObjects() {
		renderer.bindWorldObject(skyboxCube, skyboxCube.getVertices());

		renderer.bindTexturedWorldObject(geoSphere, geoSphere.getVertices(), geoSphere.getTextureCoordinates(), geoSphere.getNormals());
		// renderer.bindTexturedWorldObject(chessKingBlackGeom, chessKingBlackGeom.getVertices(), chessKingBlackGeom.getTextureCoordinates(), chessKingBlackGeom.getNormals());

		renderer.bindTexturedWorldObject(chessBoard, chessBoard.getVertices(), chessBoard.getTextureCoordinates(), chessBoard.getNormals());
		
		renderer.bindTexturedWorldObject(chessKingWhite, chessKingWhite.getVertices(), chessKingWhite.getTextureCoordinates(), chessKingWhite.getNormals());
		renderer.bindTexturedWorldObject(chessKingBlack, chessKingBlack.getVertices(), chessKingBlack.getTextureCoordinates(), chessKingBlack.getNormals());

        renderer.bindTexturedWorldObject(chessQueenBlack, chessQueenBlack.getVertices(), chessQueenBlack.getTextureCoordinates(), chessQueenBlack.getNormals());
		renderer.bindTexturedWorldObject(chessQueenWhite, chessQueenWhite.getVertices(), chessQueenWhite.getTextureCoordinates(), chessQueenWhite.getNormals());

		renderer.bindTexturedWorldObject(chessRookWhite1, chessRookWhite1.getVertices(), chessRookWhite1.getTextureCoordinates(), chessRookWhite1.getNormals());
		renderer.bindTexturedWorldObject(chessRookWhite2, chessRookWhite2.getVertices(), chessRookWhite2.getTextureCoordinates(), chessRookWhite2.getNormals());
	}

	public void display(GLAutoDrawable drawable) {
		upateElapsedTimeInfo();
		setPerspective();
		
		renderer.clearGL();
		
		if(use3DAnaglyphs) {
			anaglypsScene();
		} else {
			scene(); // the scene will will either load with 3D Anaglyphs or not
		}
	}

	public void anaglypsScene() {
		renderer.anaglyphLeftColorMask();
		computePerspectiveMatrix(-1.0);
		scene();
		renderer.anaglyphRightColorMask();
		computePerspectiveMatrix(1.0);
		scene();
	}
	// ************************* Runtime Actions *************************
	/** Renders on every frame and does actions */
	public void scene() {
		// * View Matrix from Camera
		vMat.set(camera.getViewMatrix());
		
		renderer.useCubeMapShader();
		renderSkybox();
		
		renderer.useLineShader();
		renderAxisLines();

		renderer.useLightDotShader();
		renderLightDot();
		
		renderer.prepPass1GLData();
		renderer.useMainShadowShader();
		renderWorldObjectsP1();		

		renderer.prepPass2GLData();
		renderer.useMainShader();
		renderer.setLightStatus();
		renderWorldObjectsP2();		

		renderer.useGeomAddShader();
		renderGeomShere();
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
	
	private void renderGeomShere() {
		mMat.identity();
		
		// updateGeomSphere();
		// updateChessKingBlackGeom();
		updateGeomSphere();
		renderer.setGoldMaterial();
		renderer.installLights("geomAddShader");	
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		renderer.setMVPUniformVars(mMat, vMat, pMat, invTrMat);
		renderer.renderGeomObject(geoSphere.getVBOIndex(), chessKingBlackGeom.getNumVertices(), chessKingBlackGeom.getVBONIndex());
	}

	private void updateGeomSphere() {
		geoSphere.update(elapsedTimeOffset);
		currObjLoc = geoSphere.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		geoSphere.setLocation(x, y, z);
		
		mMat.translation(x, y, z);
		mMat.scale(5f, 5f, 5f);
	}
	private void updateChessKingBlackGeom() {
		chessKingBlackGeom.update(elapsedTimeOffset);
		currObjLoc = chessKingBlackGeom.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessKingBlackGeom.setLocation(0f, 0f, 0f);
		
		mMat.translation(0f, 0f, 0f);
		mMat.scale(3.0f, 3.0f, 3.0f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);
	}

	private void renderWorldObjectsP1() {

		mMat.identity();
		updateChessBoard();
		pass1CommonActions();
		renderer.renderWorldObjectFirst(chessBoard.getVBOIndex(), chessBoard.getNumVertices(), chessBoard.getVBONIndex());

		// mMat.identity();
		// updateGeomSphere();
		// pass1CommonActions();
		// renderer.renderWorldObject(geoSphere.getVBOIndex(), geoSphere.getNumVertices(), geoSphere.getVBONIndex());

		mMat.identity();
		updateChessKingWhite();
		pass1CommonActions();
		renderer.renderWorldObject(chessKingWhite.getVBOIndex(), chessKingWhite.getNumVertices(), chessKingWhite.getVBONIndex());

		mMat.identity();
		updateChessKingBlack();
		pass1CommonActions();
		renderer.renderWorldObject(chessKingBlack.getVBOIndex(), chessKingBlack.getNumVertices(), chessKingBlack.getVBONIndex());

		mMat.identity();
		updateChessRookWhite1();
		pass1CommonActions();
		renderer.renderWorldObject(chessRookWhite1.getVBOIndex(), chessRookWhite1.getNumVertices(), chessRookWhite1.getVBONIndex());

		mMat.identity();
		updateChessRookWhite2();
		pass1CommonActions();
		renderer.renderWorldObject(chessRookWhite2.getVBOIndex(), chessRookWhite2.getNumVertices(), chessRookWhite2.getVBONIndex());
		
		mMat.identity();
		updateChessQueenWhite();
		pass1CommonActions();
		renderer.renderWorldObject(chessQueenWhite.getVBOIndex(), chessQueenWhite.getNumVertices(), chessQueenWhite.getVBONIndex());

		mMat.identity();
		updateChessQueenBlack();
		pass1CommonActions();
		renderer.renderWorldObject(chessQueenBlack.getVBOIndex(), chessQueenBlack.getNumVertices(), chessQueenBlack.getVBONIndex());
	}

	private void renderWorldObjectsP2() {
		mMat.identity();
		updateChessBoard();
		pass2CommonActions();
		renderer.renderWorldObjectFirst(chessBoard.getVBOIndex(), chessBoard.getNumVertices(), chessBoard.getVBOTxIndex(), chessBoardTexture, chessBoard.getVBONIndex());

		renderer.setGoldMaterial();
		renderer.setupLights(elapsedTimeOffset);	
		mMat.identity();
		updateChessKingWhite();
		pass2CommonActions();
		renderer.renderTexturedMaterialWorldObject(chessKingWhite.getVBOIndex(), chessKingWhite.getNumVertices(), chessKingWhite.getVBOTxIndex(), renderer.get3DMarbleTexture1(), chessKingWhite.getVBONIndex());
		
		renderer.setAmethystMaterial();
		renderer.setupLights(elapsedTimeOffset);	
		mMat.identity();
		updateChessKingBlack();
		pass2CommonActions();
		renderer.renderTexturedMaterialWorldObject(chessKingBlack.getVBOIndex(), chessKingBlack.getNumVertices(), chessKingBlack.getVBOTxIndex(), renderer.get3DMarbleTexture2(), chessKingBlack.getVBONIndex());
		
		renderer.setGoldMaterial();
		renderer.setupLights(elapsedTimeOffset);	
		mMat.identity();
		updateChessRookWhite1();
		pass2CommonActions();
		renderer.renderTexturedMaterialWorldObject(chessRookWhite1.getVBOIndex(), chessRookWhite1.getNumVertices(), chessRookWhite1.getVBOTxIndex(), renderer.get3DMarbleTexture1(), chessRookWhite1.getVBONIndex());
		// renderer.renderWorldObject(chessRookWhite1.getVBOIndex(), chessRookWhite1.getNumVertices(), chessRookWhite1.getVBONIndex());

		renderer.setGoldMaterial();
		renderer.setupLights(elapsedTimeOffset);	
		mMat.identity();
		updateChessRookWhite2();
		pass2CommonActions();
		renderer.renderTexturedMaterialWorldObject(chessRookWhite2.getVBOIndex(), chessRookWhite2.getNumVertices(), chessRookWhite2.getVBOTxIndex(), renderer.get3DMarbleTexture1(), chessRookWhite2.getVBONIndex());
		// renderer.renderWorldObject(chessRookWhite2.getVBOIndex(), chessRookWhite2.getNumVertices(), chessRookWhite2.getVBONIndex());
		
		renderer.setGoldMaterial();
		renderer.setupLights(elapsedTimeOffset);	
		mMat.identity();
		updateChessQueenWhite();
		pass2CommonActions();
		renderer.renderAlphaTexturedMaterialObject(chessQueenWhite.getVBOIndex(), chessQueenWhite.getNumVertices(), chessQueenWhite.getVBOTxIndex(), renderer.get3DMarbleTexture1(), chessQueenWhite.getVBONIndex());
		
		renderer.setAmethystMaterial();
		renderer.setupLights(elapsedTimeOffset);	
		mMat.identity();
		updateChessQueenBlack();
		pass2CommonActions();
		renderer.renderAlphaTexturedMaterialObject(chessQueenBlack.getVBOIndex(), chessQueenBlack.getNumVertices(), chessQueenBlack.getVBOTxIndex(), renderer.get3DMarbleTexture2(), chessQueenBlack.getVBONIndex());
	}

	private void pass1CommonActions() {
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		renderer.setSUniformVar(shadowMVP1);
	}

	private void pass2CommonActions() {
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		renderer.setMainShaderUniVars(mMat, vMat, pMat, invTrMat, shadowMVP2);
	}

	private void updateChessBoard() {
		chessBoard.update(elapsedTimeOffset);
		currObjLoc = chessBoard.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessBoard.setLocation(x, y, z);
		
		mMat.translation(x, y, z);
		// mMat.scale(0.75f, 0.75f, 0.75f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);

	}

	private void updateChessKingWhite() {
		chessKingWhite.update(elapsedTimeOffset);
		currObjLoc = chessKingWhite.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessKingWhite.setLocation(x, y, z);
		
		mMat.translation(x-2.5f, y, z-14f);
		// mMat.scale(0.75f, 0.75f, 0.75f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);

	}

	private void updateChessKingBlack() {
		chessKingBlack.update(elapsedTimeOffset);
		currObjLoc = chessKingBlack.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessKingBlack.setLocation(x, y, z);
		
		mMat.translation(x+2f, y, z+2.5f);
		// mMat.scale(0.75f, 0.75f, 0.75f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);

	}

	private void updateChessQueenBlack() {
		chessQueenBlack.update(elapsedTimeOffset);
		currObjLoc = chessQueenBlack.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessQueenBlack.setLocation(x, y, z);
		
		mMat.translation(x+16.0f, y, z);
		mMat.scale(3.0f, 3.0f, 3.0f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);
	}

	private void updateChessQueenWhite() {
		chessQueenWhite.update(elapsedTimeOffset);
		currObjLoc = chessQueenWhite.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessQueenWhite.setLocation(x, y, z);
		
		mMat.translation(x-9.0f, y, z);
		mMat.scale(3.0f, 3.0f, 3.0f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);
	}

	private void updateChessRookWhite1() {
		chessRookWhite1.update(elapsedTimeOffset);
		currObjLoc = chessRookWhite1.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessRookWhite1.setLocation(x, y, z);
		
		mMat.translation(x-11f, y, z-2f);
		// mMat.scale(0.75f, 0.75f, 0.75f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);

	}

	private void updateChessRookWhite2() {
		chessRookWhite2.update(elapsedTimeOffset);
		currObjLoc = chessRookWhite2.getLocation();

		x = currObjLoc.x();
		y = currObjLoc.y();
		z = currObjLoc.z();
		
		chessRookWhite2.setLocation(x, y, z);
		
		mMat.translation(x+5f, y, z);
		// mMat.scale(0.75f, 0.75f, 0.75f);
		// mMat.rotate((float)Math.toRadians(90.0f),0.0f, 1.0f, 0.0f);

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
			case KeyEvent.VK_Y:
				System.out.println("Y key pressed");
				toggle3DAnaglyphs();
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

	private void toggle3DAnaglyphs() { use3DAnaglyphs = use3DAnaglyphs ? false : true;}

	// **************** Main ****************************
	public static void main(String[] args) { new Code(); }
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupShadowBuffers();
	}
}