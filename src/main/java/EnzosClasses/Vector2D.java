package EnzosClasses;

public class Vector2D {
    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2D add(Vector2D v) {
        y += v.getY();
        x += v.getX();
        return this;
    }

    public Vector2D subtract(Vector2D v) {
        x -= v.getX();
        y -= v.getY();
        return this;
    }

    public Vector2D multiply(double m) {
        x *= m;
        y *= m;
        return this;
    }

    public Vector2D divide(double m) {
        x /= m;
        y /= m;
        return this;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D normalize() {
        double m = magnitude();
        return new Vector2D(x / m, y / m);
    }

    public double length() {
        return magnitude();
    }

    public double dotProduct(Vector2D v) {
        return this.x * v.getX() + this.y * v.getY();
    }
}
