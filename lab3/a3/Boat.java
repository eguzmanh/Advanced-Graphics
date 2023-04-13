package a3;

import com.jogamp.opengl.util.texture.*;

import org.joml.*;


// World object that is loaded from an OBJ file and also uses a special texture file
public class Boat extends ImportedWorldObject {
	private float zOff, zDir;

    public Boat(String shapeType, ImportedModel boat) {
        super(shapeType, boat);

        setLocation(-2.0f, 0f, 3.0f);

		zOff = 0.01f;
		zDir = 1.0f;
    }


	/** adjusts the zOffset for the dophin */
	public void validateZDirection() {
		if (getLocation().z() >= 3.0f) zDir = -1;
		if (getLocation().z() <=  -15.0f) zDir = 1;
	}

	// /** Get the z offset to move the dolphin */
	// public float getZOffset() {
	// 	return getSpeed() * zDir;
	// }

	/** Get the direction of the dolphin to ensure correct movement (range is [-1,1] ) */
	public float getZDirection() {
		return zDir;
	}

	@Override
    public void update(float elapsedTimeOffset) {
		setSpeed(elapsedTimeOffset);
		validateZDirection();
		// adjustZOffset();

		// setLocation(getLocation().x(), getLocation().y(), getLocation().z() + zOff);
	}
}
