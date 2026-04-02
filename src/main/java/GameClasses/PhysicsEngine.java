package GameClasses;

import java.util.List;

public class PhysicsEngine {
    private List<Token> tokens;
    private double dt = 0.016; // 60 frames per second translation
    private double gravity = 9.81;

    public PhysicsEngine(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void update(double tpf) {
        this.dt = tpf;
        updatePositions();

        for (int i = 0; i < tokens.size(); i++) {
            for (int j = i + 1; j < tokens.size(); j++) {
                Token a = tokens.get(i);
                Token b = tokens.get(j);

                if (detectCollision(a, b)) {
                    resolveCollision(a, b);
                }
            }
        }
    }

    public void updatePositions() {
        for (Token token : tokens) {
            Vector2D velocity = token.getVelocity();
            Vector2D acceleration = token.getAcceleration();

            if (velocity == null)
                velocity = new Vector2D(0, 0);
            if (acceleration == null)
                acceleration = new Vector2D(0, 0);

            Vector2D newVelocity = velocity.add(acceleration.multiply(dt));

            // Apply the fixed friction logic
            newVelocity = applyFriction(token, newVelocity);

            Vector2D newPosition = token.getPosition().add(newVelocity.multiply(dt));

            token.setPosition(newPosition);
            token.setVelocity(newVelocity);

            token.setAcceleration(new Vector2D(0, 0));
        }
    }

    public boolean detectCollision(Token t1, Token t2) {
        Vector2D posA = t1.getPosition();
        Vector2D posB = t2.getPosition();

        double xdistances = posA.getX() - posB.getX();
        double ydistances = posA.getY() - posB.getY();
        double distance = Math.sqrt(xdistances * xdistances + ydistances * ydistances);

        double sumOfRadii = t1.getRadius() + t2.getRadius();

        return distance <= sumOfRadii;
    }

    private void resolveCollision(Token a, Token b) {
        Vector2D normal = a.getPosition().subtract(b.getPosition());
        double distance = Math.sqrt(normal.getX() * normal.getX() + normal.getY() * normal.getY());

        if (distance == 0.0)
            return;

        Vector2D unitNormal = normal.normalize();

        // Positional Correction
        double overlap = (a.getRadius() + b.getRadius()) - distance;
        if (overlap > 0) {
            Vector2D correction = unitNormal.multiply(overlap / 2.0);
            a.setPosition(a.getPosition().add(correction));
            b.setPosition(b.getPosition().subtract(correction));
        }

        Vector2D relativeVelocity = a.getVelocity().subtract(b.getVelocity());
        double speedIntoCrash = relativeVelocity.dotProduct(unitNormal);

        if (speedIntoCrash > 0)
            return;

        double massA = a.getWeight() / 9.81;
        double massB = b.getWeight() / 9.81;

        double bounciness = 1.0;
        double J = -(1.0 + bounciness) * speedIntoCrash;
        J = J / ((1.0 / massA) + (1.0 / massB));

        Vector2D impulseVector = unitNormal.multiply(J);

        Vector2D deltaVa = impulseVector.multiply(1.0 / massA);
        Vector2D deltaVb = impulseVector.multiply(-1.0 / massB);

        a.setVelocity(a.getVelocity().add(deltaVa));
        b.setVelocity(b.getVelocity().add(deltaVb));
    }

    public Vector2D applyFriction(Token token, Vector2D currentv) {
        // 1. Get the current actual speed (Magnitude) using Pythagoras
        double currentSpeed = Math.sqrt((currentv.getX() * currentv.getX()) + (currentv.getY() * currentv.getY()));

        // 2. Stop completely if it's barely creeping along
        if (currentSpeed < 5.0) {
            return new Vector2D(0, 0);
        }

        // 3. Define the grass stickiness! (Pixels lost per second)
        // Adjust this number to make the grass more slippery (e.g., 100) or stickier
        // (e.g., 400)
        double frictionDeceleration = 200.0;

        // 4. Calculate exactly how much speed to lose this specific frame
        double speedToLoseThisFrame = frictionDeceleration * dt;

        // 5. If it's about to lose more speed than it currently has, just stop it
        if (currentSpeed <= speedToLoseThisFrame) {
            return new Vector2D(0, 0);
        }

        // 6. Calculate the new speed and scale the X and Y velocities perfectly
        double newSpeed = currentSpeed - speedToLoseThisFrame;
        double scale = newSpeed / currentSpeed;

        // Return the new, smoothly slowed-down vector!
        return new Vector2D(currentv.getX() * scale, currentv.getY() * scale);
    }
}