package org.example.footyclash.TestingClasses;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;

import EnzosClasses.PhysicsEngine;
import EnzosClasses.Token;
import EnzosClasses.Vector2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import java.util.ArrayList;
import java.util.List;

public class TokenDragTest extends GameApplication {

    private PhysicsEngine physicsEngine;
    private List<Token> tokens;

    // Variables to track where the token is when we start aiming
    private double startDragX;
    private double startDragY;

    // The simple, visible aiming line
    private Line aimLine;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("FootyClash: Slingshot Aim Test");
        settings.setVersion("1.0");
    }

    @Override
    protected void initGame() {
        tokens = new ArrayList<>();

        // Create the aiming line (Black and thick so it is clearly visible)
        aimLine = new Line();
        aimLine.setStroke(Color.BLACK);
        aimLine.setStrokeWidth(5);
        aimLine.setVisible(false); // Hidden until you click

        // 1. Create Token A (The Striker)
        Token tokenA = new Token();
        tokenA.setRadius(30);
        tokenA.setFill(Color.BLUE);
        tokenA.setPosition(new Vector2D(200, 300));
        tokenA.setVelocity(new Vector2D(0, 0));
        setupDragEvents(tokenA);
        tokens.add(tokenA);

        // 2. Create Token B (The Defender)
        Token tokenB = new Token();
        tokenB.setRadius(30);
        tokenB.setFill(Color.RED);
        tokenB.setPosition(new Vector2D(600, 300));
        tokenB.setVelocity(new Vector2D(0, 0));
        setupDragEvents(tokenB);
        tokens.add(tokenB);

        // Add visuals to the screen (Line goes first so it draws UNDER the tokens)
        FXGL.getGameScene().addUINode(aimLine);
        FXGL.getGameScene().addUINode(tokenA);
        FXGL.getGameScene().addUINode(tokenB);

        // Boot up your physics engine
        physicsEngine = new PhysicsEngine(tokens);
    }

    @Override
    protected void onUpdate(double tpf) {
        // Run physics math
        physicsEngine.update(tpf);

        // Keep tokens inside the window
        checkBorders();

        // Sync visual JavaFX Circles with custom Vector2D math
        for (Token t : tokens) {
            t.setCenterX(t.getPosition().getX());
            t.setCenterY(t.getPosition().getY());
        }
    }

    // --- SLINGSHOT MECHANICS ---
    private void setupDragEvents(Token token) {

        token.setOnMousePressed((MouseEvent e) -> {
            // Anchor the start of the line to the center of the token
            startDragX = token.getPosition().getX();
            startDragY = token.getPosition().getY();

            aimLine.setStartX(startDragX);
            aimLine.setStartY(startDragY);
            aimLine.setEndX(e.getSceneX());
            aimLine.setEndY(e.getSceneY());

            aimLine.setVisible(true); // Show the line

            token.setVelocity(new Vector2D(0, 0)); // Stop the token from rolling
        });

        token.setOnMouseDragged((MouseEvent e) -> {
            // Move the end of the line to follow the mouse
            aimLine.setEndX(e.getSceneX());
            aimLine.setEndY(e.getSceneY());
        });

        token.setOnMouseReleased((MouseEvent e) -> {
            aimLine.setVisible(false); // Hide the line when you shoot

            // Calculate the slingshot pull (pull back to shoot forward)
            double forceX = startDragX - e.getSceneX();
            double forceY = startDragY - e.getSceneY();

            // INSTANT SPEED: Set the velocity directly so it shoots off immediately!
            Vector2D flickVelocity = new Vector2D(forceX * 5, forceY * 5);
            token.setVelocity(flickVelocity);
        });
    }

    // --- BORDER COLLISIONS ---
    private void checkBorders() {
        double screenWidth = FXGL.getAppWidth();
        double screenHeight = FXGL.getAppHeight();

        for (Token t : tokens) {
            Vector2D pos = t.getPosition();
            Vector2D vel = t.getVelocity();
            double r = t.getRadius();

            double newPosX = pos.getX();
            double newPosY = pos.getY();
            double newVelX = vel.getX();
            double newVelY = vel.getY();

            if (pos.getX() - r < 0) {
                newPosX = r;
                newVelX = Math.abs(vel.getX());
            } else if (pos.getX() + r > screenWidth) {
                newPosX = screenWidth - r;
                newVelX = -Math.abs(vel.getX());
            }

            if (pos.getY() - r < 0) {
                newPosY = r;
                newVelY = Math.abs(vel.getY());
            } else if (pos.getY() + r > screenHeight) {
                newPosY = screenHeight - r;
                newVelY = -Math.abs(vel.getY());
            }

            t.setPosition(new Vector2D(newPosX, newPosY));
            t.setVelocity(new Vector2D(newVelX, newVelY));
        }
    }
}