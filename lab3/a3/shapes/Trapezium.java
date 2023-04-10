package a3.shapes;

import a3.WorldObject;

public class Trapezium extends WorldObject {
    private float rotationAngle;
    private float translationOffset;

    private float[] vertices = {
        -1f,0f,0f, 1f,0f,0f, 0f,1f,0f,
        0f,1f,0f, 1f,0f,0f, 1f,0f,1f,
        1f,0f,1f, 0f,1f,1f, 0f,1f,0f,
        0f,1f,0f, 0f,1f,1f, -1f,0f,1f,
        -1f,0f,1f, 0f,1f,1f, -1f,0f,0f,
        -1,0f,0f, 1f,0f,0f, -1f,0f,1f,
        -1f,0f,1f, 1f,0f,0f, 1f,0f,1f,
        1f,0f,1f, -1f,0f,1f, 0f,1f,1f,

    };
    // private float[] vertices = {
    //     -1f,0f,0f, 0f,1f,0f, 1f,0f,0f,
    //      1f,0f,0f, 1f,0f,1f, 1f,0f,-1f,
    //     1f,0f,-1f, -1f,0f,0f, 1f,0f,0f,
    //      1f,0f,0f,  1f,0f,1f, 1f,1f,1f
    // };


    public Trapezium(String shapeType) {
        super(shapeType);
        setLocation(-.5f,1.52f,0f);
        setVertices(vertices);
        setNumVertices(24);
        rotationAngle = 0f;
        translationOffset = 0f;

    }

    public void action() {}
    
    public void updateAction(float speed) {
        rotationAngle += (float)speed % 360;
    }

    public float getRotAngle() {
        return rotationAngle;
    }
    
}
