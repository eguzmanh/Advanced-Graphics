package a3.shapes;

import a3.ImportedWorldObject;
import a3.ImportedModel;

// import a3.WorldObject;

import com.jogamp.opengl.util.texture.*;

public class Pyramid extends ImportedWorldObject {

    // private float rotationAmnt;

    // vertices of a pyramid (retrieved from source)
    // private float[] vertices = {	
    //     -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,   //front
    //     1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,   //right
    //     1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, //back
    //     -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, //left
    //     -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
    //     1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
	// };
    
    /* Brick texture coordinates fo the Pyramid */
    // private float[] brickTextureCoordinates = {	
    //     0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
    //     0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
    //     0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
    //     0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
    //     0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
    //     1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
	// };

    // /* Ice texture coordinates fo the Pyramid */
    // private float[] iceTextureCoordinates = {	
    //     0.0f, 0.0f, 0.25f, 0.25f, 0.5f, 0.0f,
    //     0.0f, 0.0f, 0.25f, 0.25f, 0.5f, 0.0f,
    //     0.0f, 0.0f, 0.25f, 0.25f, 0.5f, 0.0f,
    //     0.0f, 0.0f, 0.25f, 0.25f, 0.5f, 0.0f,
    //     0.0f, 0.0f, 0.25f, 0.25f, 0.5f, 0.0f,
    //     0.0f, 0.0f, 0.25f, 0.25f, 0.5f, 0.0f,
	// };
	
	
     /* Defines a World Object with specific texture coordinates */
     public Pyramid(String shapeType, ImportedModel pyr) {
        super(shapeType, pyr);
        // setVertices(vertices);
        // setTextureCoordinates(brickTextureCoordinates);
        // setNumVertices(18);
        setLocation(1f, 2f, 0f);
        // rotationAmnt = 0.0f;

        // setVBOIndex(renderer.getCurrVBOIndex() - 1);
        // setVBOTxIndex(renderer.getCurrVBOIndex());
    }

    public void update(float elapsedSpeed) { 
        setSpeed(elapsedSpeed);
        setRotationAngle(elapsedSpeed);
    }
    
    // public void updateAction(float speed) {
    //     rotationAmnt += (float)speed % 360;
    // }

    // public void updateRotAngle() {
    //     rotationAmnt += getSpeed() % 360; 
    // }

    // public float getRotAmnt() {
    //     return rotationAmnt;
    // }
    
    // public float[] getBrickTexCoords() {
    //     return brickTextureCoordinates;
    // }
    // public float[] getIceTexCoords() {
    //     return iceTextureCoordinates;
    // }
}
