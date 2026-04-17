package GameClasses;

import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.footyclash.TestingClasses.TokenDragTest;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class Walls {

    public static void createWall(double x, double y, double w, double h) {
        entityBuilder()
                .type(TokenDragTest.EntityType.WALL)
                .at(x, y)
                .viewWithBBox(new Rectangle(w, h, Color.TRANSPARENT))
                .with(new PhysicsComponent())
                .buildAndAttach();
    }
}
