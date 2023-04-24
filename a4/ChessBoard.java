package a4;

// public class ChessBoard {
    
// }

// // World object that is loaded from an OBJ file and also uses a special texture file
public class ChessBoard extends ImportedWorldObject {
	private float zOff, zDir, xDir;

    public ChessBoard(String shapeType, ImportedModel obj) {
        super(shapeType, obj);
        // dolphin = new ImportedModel("assets/models/dolphinHighPoly.obj");
        // setupVertices();
        setLocation(0f, 0f, 0f);

		zOff = 0.01f;
		zDir = 1.0f;
		xDir = 1.0f;
    }


	/** adjusts the zOffset for the dophin */
	public void validateZDirection() {
		if (getLocation().z() >= 0f) zDir = -1;
		if (getLocation().z() <= -15f) zDir = 1;
	}

	/** adjusts the zOffset for the dophin */
	public void validateXDirection() {
		if (getLocation().x() >= 15f) xDir = -1;
		if (getLocation().x() <= 0f) xDir = 1;
	}

	// /** Get the z offset to move the dolphin */
	// public float getZOffset() {
	// 	return getSpeed() * zDir;
	// }

	/** Get the direction of the dolphin to ensure correct movement (range is [-1,1] ) */
	public float getZDirection() {
		return zDir;
	}

	/** Get the direction of the dolphin to ensure correct movement (range is [-1,1] ) */
	public float getXDirection() {
		return xDir;
	}

	@Override
    public void update(float elapsedTimeOffset) {
		setSpeed(elapsedTimeOffset);
		validateZDirection();
		validateXDirection();
		// adjustZOffset();

		// setLocation(getLocation().x(), getLocation().y(), getLocation().z() + zOff);
	}
}
