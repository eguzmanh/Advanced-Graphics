package a3;

public class ManualFoodStation extends WorldObject {
    private float rotationAngle;
    private float translationOffset;

    float[] vertices = { 
        -1.0f,  1.50f, -1.0f,    -1.0f, -1.50f, -1.0f,     1.0f, -1.50f, -1.0f, // back face lower left
        1.0f, -1.50f, -1.0f,     1.0f,  1.50f, -1.0f,    -1.0f,  1.50f, -1.0f, // back face upper right
        1.0f, -1.50f, -1.0f,     1.0f, -1.50f,  1.0f,     1.0f,  1.50f, -1.0f, // right face lower back
        1.0f, -1.50f,  1.0f,     1.0f,  1.50f,  1.0f,     1.0f,  1.50f, -1.0f, // right face upper front
        1.0f, -1.50f,  1.0f,     -1.0f, -1.50f,  1.0f,    1.0f,  1.50f,  1.0f, // front face lower right
        -1.0f, -1.50f,  1.0f,    -1.0f,  1.50f,  1.0f,     1.0f,  1.50f,  1.0f, // front face upper left
        -1.0f, -1.50f,  1.0f,    -1.0f, -1.50f, -1.0f,    -1.0f,  1.50f,  1.0f, // left face lower front
        -1.0f, -1.50f, -1.0f,    -1.0f,  1.50f, -1.0f,    -1.0f,  1.50f,  1.0f, // left face upper back
        -1.0f, -1.50f,  1.0f,     1.0f, -1.50f,  1.0f,     1.0f, -1.50f, -1.0f, // bottom face right front
        1.0f, -1.50f, -1.0f,    -1.0f, -1.50f, -1.0f,    -1.0f, -1.50f,  1.0f, // bottom face left back
        -1.0f,  1.50f, -1.0f,     1.0f,  1.50f, -1.0f,     1.0f,  1.50f,  1.0f, // top face right back
        1.0f,  1.50f,  1.0f,    -1.0f,  1.50f,  1.0f,    -1.0f,  1.50f, -1.0f  // top face left front
      }; 
  

        // Texture coordinates messed up to show the clmap-border property for tiling
      float[] texCoords = new float[] { 
        2.0f, 1.0f,  1.0f, 0.0f,  0.0f, 2.0f,
        0.0f, 2.0f,  -1.0f, 1.0f,  1.0f, 1.0f,
  
        1.5f, -1.0f,  0.0f, 0.0f,  1.0f, 1.0f, 
        0.0f, 0.0f,  0.0f, 2.0f,  1.0f, 1.0f,
  
        2.0f, 0.0f,  0.0f, 0.0f,  1.0f, 1.0f, 
        0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f,
  
        1.0f, 0.0f,  0.0f, 0.0f,  1.0f, 1.0f, 
        0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f,
  
        0.0f, 1.0f,  1.0f, 1.0f,  1.0f, 0.0f, 
        1.0f, 0.0f,  0.0f, 0.0f,  0.0f, 1.0f,
  
        0.0f, 1.0f,  1.0f, 1.0f,  1.0f, 0.0f, 
        1.0f, 0.0f,  0.0f, 0.0f,  0.0f, 2.0f 
     };


    public ManualFoodStation(String shapeType) {
        super(shapeType);
        setLocation(0f,0f,0f);
        setVertices(vertices);
        setTextureCoordinates(texCoords);
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
    
    // public float[] getBrickTexCoords() {
    //     return brickTextureCoordinates;
    // }
    // public float[] getIceTexCoords() {
    //     return iceTextureCoordinates;
    // }
    // }
}
