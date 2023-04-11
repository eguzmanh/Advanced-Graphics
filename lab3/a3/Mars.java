package a3;

import org.joml.*;

public class Mars extends ImportedWorldObject { 
    private float xDir;

    public Mars(String shapeType, ImportedModel marsObj) {
        super(shapeType, marsObj);
        setLocation(1.5f, -0.1f, 2f);
        xDir = 1.0f;
    }

    /** adjusts the zOffset for Mars */
	public void validateXDirection() {
		if (getLocation().x() >= 7f) xDir = -1;
		if (getLocation().x() <= -7f) xDir = 1;
	}

    /** Get the direction mars to ensure correct movement (range is [-1,1] ) */
	public float getXDirection() {
		return xDir;
	}


    @Override
    public void update(float elapsedSpeed) {
        setSpeed(elapsedSpeed);
		validateXDirection();
    }
}
