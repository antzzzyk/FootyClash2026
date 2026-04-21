package GameClasses;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.footyclash.TestingClasses.TokenDragTest;
import GameClasses.CustomPhysicsComponent; // Make sure package is correct

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class Token extends Circle {

    // You can move these to your CustomPhysicsComponent later,
    // as Box2D isn't here to read them anymore!
    private static final float DENSITY = 1f;
    private static final float RESTITUTION = 0.7f;

    public Token() {
    }

    public static float getDensity() {
        return DENSITY;
    }

    public static void createToken(double x, double y, Color color, TokenDragTest.EntityType type) {

        entityBuilder()
                .type(type)
                .at(x, y)
                .viewWithBBox(new Circle(24, 24, 24, color))
                // Give the Token a mass of 5.0 and a restitution of 0.8
                .with(new CustomPhysicsComponent(5.0, 0.8))
                .collidable() // Tells FXGL to register bounding box overlaps, even without Box2D
                .buildAndAttach();
    }
}