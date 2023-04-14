package a3;

import org.joml.*;

/** Class to interact with the 3D scene/world by virtualizing the view,
 * Contains UVN vectors and a location vector
 * 
 */
public class Camera {
    private Vector3f location;
    private Vector3f u, v, n;
    private Matrix4f viewMatrix, viewRotationMatrix, viewTranslationMatrix;
    
    public Camera() {	
		location = new Vector3f(0.0f, 1.5f, 10f);
		u = new Vector3f(1.0f, 0.0f, 0.0f);
		v = new Vector3f(0.0f, 1.0f, 0.0f);
		n = new Vector3f(0.0f, 0.0f, -1.0f);
        viewMatrix = new Matrix4f();
        viewRotationMatrix = new Matrix4f();
        viewTranslationMatrix = new Matrix4f();
	}

	// ******************************* Setters ***********************************************************
    /** sets the world location of this Camera */
	public void setLocation(Vector3f l) { location.set(l); }

	/** sets the U (right-facing) vector for this Camera */
	public void setU(Vector3f newU) { u.set(newU); }

	/** sets the V (upward-facing) vector for this Camera */
	public void setV(Vector3f newV) { v.set(newV); }

	/** sets the N (forward-facing) vector for this Camera */
	public void setN(Vector3f newN) { n.set(newN); }


	// ******************************* Getters ***********************************************************
	/** returns the world location of this Camera */
	public Vector3f getLocation() { return new Vector3f(location); }

	/** gets the U (right-facing) vector for this Camera */
	public Vector3f getU() { return new Vector3f(u); }

	/** gets the V (upward-facing) vector for this Camera */
	public Vector3f getV() { return new Vector3f(v); }

	/** gets the N (forward-facing) vector for this Camera */
	public Vector3f getN() { return new Vector3f(n); }

	/** Build the View Matrix to render objects in the frustum (cannot be overwritten) */
    protected Matrix4f getViewMatrix() { 
        viewTranslationMatrix.set(1.0f, 0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f, 0.0f,
		0.0f, 0.0f, 1.0f, 0.0f,
		-location.x(), -location.y(), -location.z(), 1.0f);

		viewRotationMatrix.set(u.x(), v.x(), -n.x(), 0.0f,
		u.y(), v.y(), -n.y(), 0.0f,
		u.z(), v.z(), -n.z(), 0.0f,
		0.0f, 0.0f, 0.0f, 1.0f);

		viewMatrix.identity();
		viewMatrix.mul(viewRotationMatrix);
		viewMatrix.mul(viewTranslationMatrix);

		return viewMatrix;
    }

	// ******************************* Camera Movements ***********************************************************
	/** Move the camera forward and backward */
	public void forward(float newSpeed) {
		Vector3f oldPosition, fwdDirection, newLocation;
		oldPosition = getLocation(); 
		fwdDirection = getN(); // N vector 
		fwdDirection.mul(newSpeed*6f);
		newLocation = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z()); 
		setLocation(newLocation); 
	}

	/** Move the camera parallel to the V vetor up in positive and negative values (scale V vector */
	public void up(float newSpeed) {
		Vector3f oldPosition, upDirection, newLocation;
		
		oldPosition = getLocation(); 
		upDirection = getV(); // V vector 
		
		upDirection.mul(newSpeed);
		
		newLocation = oldPosition.add(upDirection.x(), upDirection.y(), upDirection.z()); 
		
		setLocation(newLocation); 
	}

	/** Strife the camera (scale the U vector)  */
	public void strife(float newSpeed) {
		Vector3f oldPosition, rightDirection, newLocation;
		
		oldPosition = getLocation(); 
		rightDirection = getU(); // U vector 
		
		rightDirection.mul(newSpeed);

		newLocation = oldPosition.add(rightDirection.x(), rightDirection.y(), rightDirection.z()); 
		
		setLocation(newLocation); 
	}

	/** pan the camera (routate about the Y axis) */
	public void pan(float newSpeed) {
		Vector3f cameraLocation = getLocation(); 
		Vector3f cameraU = getU(); 
		Vector3f cameraV = getV(); 
		Vector3f cameraN = getN(); 
		
		cameraU.rotateAxis(newSpeed, cameraV.x(), cameraV.y(), cameraV.z()); 
		cameraN.rotateAxis(newSpeed, cameraV.x(), cameraV.y(), cameraV.z()); 
		
		setU(cameraU);
		setN(cameraN);
	}

	/** Pitch the camera up down (rotate about the U Vector) */ 
	public void pitch(float newSpeed) {
		Vector3f cameraLocation = getLocation(); 
		Vector3f cameraU = getU(); 
		Vector3f cameraV = getV(); 
		Vector3f cameraN = getN(); 
		
		cameraV.rotateAxis(newSpeed, cameraU.x(), cameraU.y(), cameraU.z()); 
		cameraN.rotateAxis(newSpeed, cameraU.x(), cameraU.y(), cameraU.z()); 
		
		setV(cameraV);
		setN(cameraN);
	}
	
}
