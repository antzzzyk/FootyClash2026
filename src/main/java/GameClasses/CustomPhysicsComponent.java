package GameClasses;

import GameClasses.Pitch;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class CustomPhysicsComponent extends Component {

    private Point2D velocity = Point2D.ZERO;
    private final double GRAVITY = 9.8 * 10;

    // --- NEW: Physics Properties ---
    private double mass;
    private double restitution;

    // Pass these in when you create the Token/Ball!
    public CustomPhysicsComponent(double mass, double restitution) {
        this.mass = mass;
        this.restitution = restitution;
    }

    public void applyImpulse(Point2D forceVector) {
        // Because F = ma, Impulse = Force / Mass
        // A heavier token will now accelerate less from the same mouse drag!
        Point2D acceleration = forceVector.multiply(1.0 / mass);
        velocity = velocity.add(acceleration);
    }

    @Override
    public void onUpdate(double tpf) {
        if (velocity.magnitude() > 0) {
            // Apply the velocity to move the entity
            entity.translate(velocity.multiply(tpf));

            // Apply friction (damping) so the tokens eventually stop
            double damping = Math.pow(0.96, tpf * 60.0);
            velocity = velocity.multiply(damping);

            // Hard stop if moving very slowly to prevent endless micro-movements
            if (velocity.magnitude() < 5) {
                velocity = Point2D.ZERO;
            }
        }
    }

    public void setVelocity(Point2D newVelocity) {
        this.velocity = newVelocity;
    }

    public Point2D getVelocity() {
        return velocity;
    }

    // New Getters for the Collision Engine
    public double getMass() {
        return mass;
    }

    public double getRestitution() {
        return restitution;
    }
}