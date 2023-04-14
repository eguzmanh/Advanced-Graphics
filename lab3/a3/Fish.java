package a3;

import com.jogamp.opengl.util.texture.*;

import org.joml.*;


// package a3.shapes;

import a3.ImportedWorldObject;
import a3.ImportedModel;

// import a3.WorldObject;

import com.jogamp.opengl.util.texture.*;

public class Fish extends ImportedWorldObject {
	
     /* Defines a World Object with specific texture coordinates */
     public Fish(String shapeType, ImportedModel fish) {
        super(shapeType, fish);
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
}
