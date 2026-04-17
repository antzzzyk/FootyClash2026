package GameClasses;

import GameClasses.Pitch; // Import your centralized Pitch class
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class CustomPhysicsComponent extends Component {

    private Point2D velocity = Point2D.ZERO;
    private final double GRAVITY = 9.8 * 10; // Scaling factor for the game screen

    public void applyImpulse(Point2D forceVector) {
        velocity = velocity.add(forceVector);
    }

    @Override
    public void onUpdate(double tpf) {
        double speed = velocity.magnitude();

        if (speed > 0.1) {
            double mu = Pitch.getFrictioncoefficient();
            double damping = Pitch.getDampingcoefficient();

            // 1. Damping (Exponential Drag)
            // Use Math.max to prevent damping from ever turning velocity negative
            velocity = velocity.multiply(Math.max(0, 1.0 - (damping * tpf)));

            // 2. Kinetic Friction (Linear Stop)
            Point2D frictionDirection = velocity.normalize().multiply(-1);

            // Calculate EXACTLY how much speed friction wants to eat this frame
            Point2D frictionDelta = frictionDirection.multiply(mu * GRAVITY * tpf);

            // FIX: The Anti-Jitter check!
            // If our speed is smaller than the friction bite, we stop instantly.
            if (speed <= frictionDelta.magnitude()) {
                velocity = Point2D.ZERO;
            } else {
                velocity = velocity.add(frictionDelta);
            }

        } else {
            // Hard snap to zero
            velocity = Point2D.ZERO;
        }

        // --- NEW: CUSTOM WALL COLLISION ---
        double nextX = entity.getX() + (velocity.getX() * tpf);
        double nextY = entity.getY() + (velocity.getY() * tpf);
        double bounceRestitution = 0.8;

        if (nextX < 67 || nextX > 990) {
            velocity = new Point2D(-velocity.getX() * bounceRestitution, velocity.getY());
        }
        if (nextY < 60 || nextY > 500) {
            velocity = new Point2D(velocity.getX(), -velocity.getY() * bounceRestitution);
        }

        entity.translate(velocity.multiply(tpf));
    }
    // =====================================================================
    // THE PREDICTIVE MATH
    // =====================================================================

    public double getPredictedStoppingDistance(Point2D hypotheticalVelocity) {
        double speed = hypotheticalVelocity.magnitude();
        double mu = Pitch.getFrictioncoefficient();

        // Note: This kinematic formula only perfectly accounts for linear friction.
        // Because damping is exponential, calculating the exact stopping distance
        // with damping requires integral calculus. For a game prediction line,
        // using just the friction coefficient usually provides a "close enough" visual!
        return (speed * speed) / (2 * mu * GRAVITY);
    }

    public Point2D getPredictedFinalPosition(Point2D startPos, Point2D hypotheticalVelocity) {
        if (hypotheticalVelocity.magnitude() == 0) {
            return startPos;
        }

        double distance = getPredictedStoppingDistance(hypotheticalVelocity);
        Point2D direction = hypotheticalVelocity.normalize();

        return startPos.add(direction.multiply(distance));
    }

    public Point2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Point2D newVelocity) {
        this.velocity = newVelocity;
    }
}
