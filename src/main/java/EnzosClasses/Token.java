package EnzosClasses;

import javafx.scene.shape.Circle;

public class Token extends Circle {
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private double weight = 5; // N
    private double radius = 2; // M

    public Token() {
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

    public void move(Vector2D velocity, Vector2D force) {
        this.velocity = velocity;
        acceleration = (force.subtract(new Vector2D(Pitch.getFrictionCoefficient() * weight,
                Pitch.getFrictionCoefficient() * weight)).divide(weight)).multiply(-1);
    }

    public void stop() {
        velocity = acceleration = new Vector2D(0, 0);
    }

    public void applyForce(Vector2D force) {
        velocity = force;
        move(velocity, force);
    }

}
