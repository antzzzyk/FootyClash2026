package EnzosClasses;

import java.util.List;

public class PhysicsEngine {
    private List<Token> tokens;
    private double dt = 0.0016; // 60 frames per second
    private double gravity = 9.81;

    public void updatePositions() {
        for (Token token : tokens) {
            Vector2D velocity = token.getVelocity();
            Vector2D acceleration = token.getAcceleration();

            Vector2D newVelocity = velocity.add(acceleration.multiply(dt));

            newVelocity = applyFriction(token, newVelocity);

            Vector2D newPosition = token.getPosition().add(newVelocity.multiply(dt));
            token.setPosition(newPosition);
            token.setVelocity(newVelocity);

        }

    }

    // Detect Collision between two tokens, where the system checks
    // if the distance between the two tokens is less than the sum of their radii
    public boolean detectCollision(Token t1, Token t2) {
        Vector2D posA = t1.getPosition(); // Position of Token 1
        Vector2D posB = t2.getPosition(); // Position of Token 2

        // Distances
        double xdistances = posA.getX() - posB.getX();
        double ydistances = posA.getY() - posB.getY();
        double distance = Math.sqrt(xdistances * xdistances + ydistances * ydistances);

        double sumOfRadii = t1.getRadius() + t2.getRadius();

        return distance < sumOfRadii;
    }

    public void resolveCollision() {

    }

    public Vector2D applyFriction(Token token, Vector2D currentv) {
        if (currentv.length() < 0.01) {
            return new Vector2D(0, 0);
        }

        double frictionMagnitude = Pitch.getFrictionCoefficient() * (token.getWeight() / gravity) * gravity;

        Vector2D frictionVector = currentv.normalize().multiply(-frictionMagnitude);

        return currentv.add(frictionVector);

    }
}
