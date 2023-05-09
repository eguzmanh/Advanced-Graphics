package a4;

import org.joml.*;
// import org.joml.Math;
import static java.lang.Math.*;

import a4.WorldObject;


public class SpaceSphere extends WorldObject {

    // private Vector3f rotV;
    
    // private float rotationAngle;
    // private float translationAmnt;


    private int prec;
	

	public SpaceSphere(String shapeType) 
	{	
        super(shapeType);
        prec = 48;
		InitSphere();
	}
	
	public SpaceSphere(String shapeType, int p)
	{	
        super(shapeType);
        prec = p;
		InitSphere();
	}
	
	private void InitSphere()
	{	
        int numVertices, numIndices;
        int[] indices;
        Vector3f[] vertices;
        Vector2f[] texCoords;
        Vector3f[] normals;
        Vector3f[] tangents;
        
        numVertices = (prec+1) * (prec+1);
		numIndices = prec * prec * 6;

		indices = new int[numIndices];
		vertices = new Vector3f[numVertices];
		texCoords = new Vector2f[numVertices];
		normals = new Vector3f[numVertices];
		tangents = new Vector3f[numVertices];
		
		for (int i=0; i<numVertices; i++)
		{	vertices[i] = new Vector3f();
			texCoords[i] = new Vector2f();
			normals[i] = new Vector3f();
			tangents[i] = new Vector3f();
		}

		// calculate triangle vertices
		for (int i=0; i<=prec; i++)
		{	for (int j=0; j<=prec; j++)
			{	float y = (float)cos(toRadians(180-i*180/prec));
				float x = -(float)cos(toRadians(j*360/(float)prec))*(float)abs(cos(asin(y)));
				float z = (float)sin(toRadians(j*360/(float)prec))*(float)abs(cos(asin(y)));
				vertices[i*(prec+1)+j].set(x,y,z);
				texCoords[i*(prec+1)+j].set((float)j/prec, (float)i/prec);
				normals[i*(prec+1)+j].set(x,y,z);

				// calculate tangent vector
				if (((x==0) && (y==1) && (z==0)) || ((x==0) && (y==-1) && (z==0)))
				{	tangents[i*(prec+1)+j].set(0.0f, 0.0f, -1.0f);
				}
				else
				{	tangents[i*(prec+1)+j] = (new Vector3f(0,1,0)).cross(new Vector3f(x,y,z));
		}	}	}
		
		// calculate triangle indices
		for(int i=0; i<prec; i++)
		{	for(int j=0; j<prec; j++)
			{	indices[6*(i*prec+j)+0] = i*(prec+1)+j;
				indices[6*(i*prec+j)+1] = i*(prec+1)+j+1;
				indices[6*(i*prec+j)+2] = (i+1)*(prec+1)+j;
				indices[6*(i*prec+j)+3] = i*(prec+1)+j+1;
				indices[6*(i*prec+j)+4] = (i+1)*(prec+1)+j+1;
				indices[6*(i*prec+j)+5] = (i+1)*(prec+1)+j;
	}	}	

    float[] pvalues = new float[indices.length*3];
    float[] tvalues = new float[indices.length*2];
    float[] nvalues = new float[indices.length*3];
    
    for (int i=0; i<indices.length; i++)
    {	pvalues[i*3] = (float) (vertices[indices[i]]).x;
        pvalues[i*3+1] = (float) (vertices[indices[i]]).y;
        pvalues[i*3+2] = (float) (vertices[indices[i]]).z;
        tvalues[i*2] = (float) (texCoords[indices[i]]).x;
        tvalues[i*2+1] = (float) (texCoords[indices[i]]).y;
        nvalues[i*3] = (float) (normals[indices[i]]).x;
        nvalues[i*3+1]= (float)(normals[indices[i]]).y;
        nvalues[i*3+2]=(float) (normals[indices[i]]).z;
    }

    setNumVertices(numVertices);
    setNumIndices(indices.length);
    setIndices(indices);
    
    setVertices(pvalues);
    setTextureCoordinates(tvalues);
    setNormals(nvalues);
}

	// public int getNumIndices() { return numIndices; }
	// public int getNumVertices() { return numIndices; }
	// public int[] getIndices() { return indices; }
	// public Vector3f[] getVertices() { return vertices; }
	// public Vector2f[] getTexCoords() { return texCoords; }
	// public Vector3f[] getNormals() { return normals; }
	// public Vector3f[] getTangents() { return tangents; }
}