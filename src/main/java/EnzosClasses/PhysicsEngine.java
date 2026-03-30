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

    // Impulse Logic
    private void resolveCollision(Token a, Token b) {
        // 1. Find the exact direction of the crash (The Normal line)
        Vector2D normal = a.getPosition().subtract(b.getPosition());
        Vector2D unitNormal = normal.normalize(); // A vector with a length of exactly 1

        // 2. Find out how fast they are smashing INTO each other
        Vector2D relativeVelocity = a.getVelocity().subtract(b.getVelocity());
        double speedIntoCrash = relativeVelocity.dotProduct(unitNormal);

        // If they are already moving apart, do nothing!
        if (speedIntoCrash > 0)
            return;

        // 3. Get the masses (Mass = Weight / 9.81)
        double massA = a.getWeight() / 9.81;
        double massB = b.getWeight() / 9.81;

        // 4. Calculate 'J' (The total Oomph)
        // This is the 2D game physics version of finding J for two bouncy objects
        double bounciness = 1.0; // 1.0 means no energy is lost
        double J = -(1.0 + bounciness) * speedIntoCrash;
        J = J / ((1.0 / massA) + (1.0 / massB));

        // 5. Turn J into a Vector (Direction + Oomph)
        Vector2D impulseVector = unitNormal.multiply(J);

        // 6. Apply your formula: delta v = J / m
        Vector2D deltaVa = impulseVector.multiply(1.0 / massA);
        Vector2D deltaVb = impulseVector.multiply(-1.0 / massB); // Negative because it goes the opposite way!

        // 7. Give the tokens their new speeds
        a.setVelocity(a.getVelocity().add(deltaVa));
        b.setVelocity(b.getVelocity().add(deltaVb));
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
