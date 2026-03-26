package EnzosClasses;

import javafx.scene.shape.Circle;

public class Token extends Circle {
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private double mass = 5;
    private double radius = 2;

    public Token() {
    }

    public Vector2D getPosition() {
        return position;
    }
    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public double getMass(){
        return mass;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    public void move(Vector2D velocity) {
        this.velocity = velocity;
        acceleration =
    }

    public void stop() {
        velocity = acceleration = new Vector2D(0, 0);
    }

    public void applyForce(Vector2D force) {
        velocity = force;
        move(velocity);
    }

}
