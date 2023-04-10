package a3.shapes;

import a3.WorldObject;
import org.joml.*;
import org.joml.Math;


public class Cube extends WorldObject {

    // private Vector3f rotV;
    
    // private float rotationAngle;
    // private float translationAmnt;
    private float yDir;

    float[] vertices = { 
        -1.0f,  1.0f, -1.0f,  -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f, // back face lower left
         1.0f, -1.0f, -1.0f,   1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f, // back face upper right
         1.0f, -1.0f, -1.0f,   1.0f, -1.0f,  1.0f,  1.0f,  1.0f, -1.0f, // right face lower back
         1.0f, -1.0f,  1.0f,   1.0f,  1.0f,  1.0f,  1.0f,  1.0f, -1.0f, // right face upper front
         1.0f, -1.0f,  1.0f,  -1.0f, -1.0f,  1.0f,  1.0f,  1.0f,  1.0f, // front face lower right
        -1.0f, -1.0f,  1.0f,  -1.0f,  1.0f,  1.0f,  1.0f,  1.0f,  1.0f, // front face upper left
        -1.0f, -1.0f,  1.0f,  -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f, // left face lower front
        -1.0f, -1.0f, -1.0f,  -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f, // left face upper back
        -1.0f, -1.0f,  1.0f,   1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f, // bottom face right front
         1.0f, -1.0f, -1.0f,  -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, // bottom face left back
        -1.0f,  1.0f, -1.0f,   1.0f,  1.0f, -1.0f,  1.0f,  1.0f,  1.0f, // top face right back
         1.0f,  1.0f,  1.0f,  -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f, // top face left front
    };

    // custom texture coordinates used for the cube with the artsy texture image
    private float[] artsyTextureCoordinates = {	
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // back face
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // right face
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // front face
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // left face
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // bottom face
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // top face
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
    };

    float[] normals = new float[]
	{ 0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f, // back face
	0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,  0.0f, 0.0f, -1.0f,
	1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f, // right face
	1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f,
	0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f, // front face
	0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 1.0f,
	-1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f, // left face
	-1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,  -1.0f, 0.0f, 0.0f,
	0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f, // bottom face
	0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,  0.0f, -1.0f, 0.0f,
	0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f, // top face
	0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f };

     /* Defines a World Object with specific texture coordinates */
     public Cube(String shapeType) {
        super(shapeType);
        setVertices(vertices);
        setNumVertices(36);
        setNormals(normals);
        setLocation(3f, 0f, 3.0f);
        // rotationAngle = 0;
        yDir = 1.0f;
    }

    @Override
    public void update(float elapsedOffset, float min, float max) {
        setSpeed(elapsedOffset);
        validateYDirection(min, max);
        setRotationAngle(elapsedOffset);
    }

    /** Updates the Translation Offset only  */
    public void validateYDirection(float min, float max) {
        if(getLocation().y() >= max) { yDir = -1.0f; }
        if(getLocation().y() <= min) { yDir = 1.0f; }
    }

    /** Get the translation offset */
    public float getYDir() {
        return yDir;
    }

    
    /** Used by cubes to retrieve the texture coordinates chosen for the arsty image */
    public float[] getArtsyTextureCoordinates() {
        return artsyTextureCoordinates;
    }
}