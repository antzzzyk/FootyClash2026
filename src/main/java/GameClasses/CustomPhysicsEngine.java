package GameClasses;

import com.almasb.fxgl.physics.PhysicsComponent;

public class CustomPhysicsEngine {

    // --- 1. The Impulse Formula (Input) ---
    // Calculates the required force based on drag distance
    // Impulse = Force * distance
    public static double calculateDrag(double dragdistance, double forcemultiplier) {
        return dragdistance * forcemultiplier;
    }

    // 2. Damping Formula
    // Calculates how far the object will slide while accouting damping
    // Result in pixels
    public static double StoppingDistance(double mass, double force, double damping, double pixelsPerMeter) {
        if (damping <= 0) {
            damping = 0.0001;
        }
        double initialVelocity = force / mass;
        return (initialVelocity / damping) * pixelsPerMeter;
    }

    // 3. Kinetic Energy
    // Checks if a specific physics component is moving fast enough to matter
    public static boolean isMoving(PhysicsComponent physics, double velocityThreshold) {
        double vx = physics.getVelocityX();
        double vy = physics.getVelocityY();

        // a^2 + b^2 = c^2 (comparing squared magnitudes is faster than Math.sqrt)
        return (vx * vx + vy * vy) > (velocityThreshold * velocityThreshold);
    }

    // 4.

}
