package a4;

import com.jogamp.opengl.util.texture.*;

import org.joml.*;


// World object that is loaded from an OBJ file and also uses a special texture file
public class WaterTank extends ImportedWorldObject {

    public WaterTank(String shapeType, ImportedModel boat) {
        super(shapeType, boat);

        setLocation(5f, 0f, -6f);
    }

	@Override
    public void update(float elapsedTimeOffset) {
		setSpeed(elapsedTimeOffset);
	}
}
