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

    public void detectCollision() {

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
