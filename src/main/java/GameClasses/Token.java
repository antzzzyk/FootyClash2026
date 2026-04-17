package GameClasses;

import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.footyclash.TestingClasses.TokenDragTest;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;

public class Token extends Circle {

    private static final float DENSITY = 1f;
    private static final float ANGULARDAMPING = 2f;
    private static final float RESTITUTION = 0.7f;

    // FIX 1: Initialize these immediately to prevent the NullPointerException!
    private Vector2D position = new Vector2D(0, 0);
    private Vector2D velocity = new Vector2D(0, 0);
    private Vector2D acceleration = new Vector2D(0, 0);

    // Treating this as Mass (kg) for your F=ma calculations
    private double weight = 5;
    private double radius = 2;

    public Token() {
    }

    public static float getDensity() {
        return DENSITY;
    }

    public static void createToken(double x, double y, Color color, TokenDragTest.EntityType type) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().density(DENSITY).restitution(RESTITUTION).friction(Pitch.getFrictioncoefficient()));

        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setLinearDamping(Pitch.getDampingcoefficient());
            physics.getBody().setAngularDamping(ANGULARDAMPING);
            physics.getBody().setSleepingAllowed(true);
        });

        entityBuilder()
                .type(type)
                .at(x, y)
                .viewWithBBox(new Circle(24, 24, 24, color))
                .with(physics)
                .collidable()
                .buildAndAttach();
    }
    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    public double getWeight() {
        return weight;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    // FIX 2: Newton's 2nd Law! (Acceleration = Force / Mass)
    // The PhysicsEngine will handle the actual movement later.
    public void applyForce(Vector2D force) {
        // We use 'weight' here since it represents your 5kg mass
        this.acceleration = force.divide(weight);
    }

    public void stop() {
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
    }

    // NOTE: I completely removed your old move() method!
    // Why? Because PhysicsEngine.updatePositions() now does all the
    // kinematics and friction math for you every single frame.
}