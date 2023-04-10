package a3;

import org.joml.*;

public class ImportedWorldObject extends WorldObject {

    public ImportedWorldObject(String shapeType, ImportedModel obj) {
        super(shapeType);
        setNumVertices(obj.getNumVertices());
        setupVertices(obj);
    }
    
    private void setupVertices(ImportedModel obj) {	
		int numVertices = getNumVertices();
		Vector3f[] tempVertices = obj.getVertices();
		Vector2f[] tempTexCoords = obj.getTexCoords();
		Vector3f[] normals = obj.getNormals();
		
		float[] pvalues = new float[numVertices*3];
		float[] tvalues = new float[numVertices*2];
        float[] nvalues = new float[numVertices*3];
        
		// float[] nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numVertices; i++)
		{	pvalues[i*3]   = (float) (tempVertices[i]).x();
			pvalues[i*3+1] = (float) (tempVertices[i]).y();
			pvalues[i*3+2] = (float) (tempVertices[i]).z();
			tvalues[i*2]   = (float) (tempTexCoords[i]).x();
			tvalues[i*2+1] = (float) (tempTexCoords[i]).y();
            nvalues[i*3]   = (float) (normals[i]).x();
			nvalues[i*3+1] = (float) (normals[i]).y();
			nvalues[i*3+2] = (float) (normals[i]).z();
		}
        
        setVertices(pvalues);
        setTextureCoordinates(tvalues);
        setNormals(nvalues);
	}

    @Override
    public void update(float elapsedSpeed) {}
}
