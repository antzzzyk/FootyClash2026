package GameClasses;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.footyclash.TestingClasses.TokenDragTest;
import GameClasses.CustomPhysicsComponent;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class Ball extends Token {

    private static final float DENSITY = 0.2f;
    private static final float RESTITUTION = 0.9f;

    public static void createBall(double x, double y) {

        Circle ballShape = new Circle(16, 16, 16, Color.WHITE);
        ballShape.setStroke(Color.BLACK);
        ballShape.setStrokeWidth(3);

        entityBuilder()
                .type(TokenDragTest.EntityType.BALL)
                .at(x, y)
                .viewWithBBox(ballShape)
                .with(new CustomPhysicsComponent()) // 100% Custom Physics
                .collidable()
                .buildAndAttach();
    }
}