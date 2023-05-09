package a4;

import org.joml.*;
/*
 * What should an World Object have?
    * location: location in the 3D scene (vec3)
    * rotationAngle: angle to rotate the object
    * translationOffsets 
    * UVN vectors (figure out how to give objects these vectors)

 * What should an World Object Shape have?
    * shapeType: description of the shape 
    * vertices: vertices for the object
    * textureCoordinates: coordinates that align with the object vertices
    * numberVertices: number of vertices for the object 
 */

/** Object class to assign common data, like speed and location to objects appearing in the 3D Scene */
public abstract class WorldObject extends ModelObject {
    // private String name;
    private String shape;
    private boolean enabled;
    private Vector3f location;

    private int vboIndex, vboTxIndex, vboNIndex, vboIdxIndex;

    private float rotationAngle;

    // private float xDir, yDir, zDir;
    
    private float speed;

    /* Defines a World Object without specific texture coordinates */
    public WorldObject(String shapeType) {
        super(shapeType);
        enabled = true;
        speed = 0.0f;
        rotationAngle = 0f;
    }

    /** Gets the vec3 location of the object */
    public Vector3f getLocation() { return location; }
    public float getSpeed() { return speed; }
    public int getVBOIndex() { return vboIndex; }
    public int getVBOTxIndex() { return vboTxIndex; }
    public int getVBONIndex() { return vboNIndex; }
    public int getVBONIdxIndex() { return vboIdxIndex; }

    public float getRotationAngle() { return rotationAngle; }

    public boolean enabled() { return enabled; }

    /** If floats are passed in, send a vector to the real setLocation() */
    public void setLocation(float x, float y, float z) { setLocation(new Vector3f(x,y,z)); }

    /** Sets the location of the object in the 3D scene */
    public void setLocation(Vector3f newLocation) { location = newLocation; }

    public void setSpeed(float newSpeed) { speed = newSpeed; }

    public void setVBOIndex(int vboi) { vboIndex = vboi; }

    public void setVBOTxIndex(int vboi) { vboTxIndex = vboi; }

    public void setVBONIndex(int vboi) { vboNIndex = vboi; }

    public void setVBOIdxIndex(int vboi) { vboIdxIndex = vboi; }

    public void setRotationAngle(float offset) { rotationAngle += offset % 360; };

    public void enable() { enabled = true; }

    public void disable() { enabled = false; }

    public void toggleEnableFlag() { enabled = enabled ? false : true; }

    /** Curerrently doesn't do anything and can be overwritten
     * The idea behind teh function is that at one point we can call it and it will execute an action the object defines
     */
    public void update(float elapsedSpeed) {}
    public void update(float elapsedSpeed, float min, float max) {}
}
