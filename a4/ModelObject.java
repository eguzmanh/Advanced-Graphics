package a4;

import org.joml.*;

/** Used to generate an object */
public class ModelObject {
    private String shapeType;
    private int numVertices, numIndices;
    private int[] indices;
    
    private float[] vertices, textureCoordinates, normals, tangents; 
    // private int normals;

    public ModelObject(String objType) {
        shapeType = objType;
    }

    // ******************************* Getters ***********************************************************
    /** Stores the shape type (can be assigned to any value dev would like for their identifiers) */
    public String getShapeType() { return shapeType; }

    /** Gets the object vertices when they are set */
    public float[] getVertices() { return vertices; }

    public int[] getIndices() { return indices; }

    /** Gets the object texture coordinates when they are set */
    public float[] getTextureCoordinates() { return textureCoordinates; }
   
    public float[] getNormals() { return normals; }
    
    public float[] getTangents() { return tangents; }


    /** get the number of vertices
     *  This function is usually called only by models that implemenet indices
     * @return numVertices
     */
    public int getNumVertices() { return numVertices; }
    public int getNumIndices() { return numIndices; }

    
    // ******************************* Setters ***********************************************************
    /** Sets the vertices of the object */
    public void setVertices(float[] newVertices) { vertices = newVertices; }
   
    public void setIndices(int[] newIndices) { indices = newIndices; }

    /** Sets the texture coordinates assigned to the object */
    public void setTextureCoordinates(float[] newTextureCoordinates) { textureCoordinates = newTextureCoordinates; }

    public void setNormals(float[] newNormals) { normals = newNormals; }
    
    public void setTangents(float[] newTangents) { tangents = newTangents; }

    /** set the number of vertices 
     * use this function when doing index-based models
    */
    public void setNumVertices(int numV) { numVertices = numV; }
    public void setNumIndices(int numI) { numIndices= numI; }


}
