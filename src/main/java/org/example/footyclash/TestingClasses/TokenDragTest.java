package org.example.footyclash.TestingClasses;

import GameClasses.Ball;
import GameClasses.Pitch;
import GameClasses.Token;
import GameClasses.Walls;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import GameClasses.CustomPhysicsComponent;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class TokenDragTest extends GameApplication {

    public enum EntityType {
        TEAM_BLUE, TEAM_RED, BALL, WALL
    }

    private static String[] launchArgs;

    private static final double MIN_VELOCITY_THRESHOLD = 100.0;
    private static final double MAX_DRAG_DISTANCE = 150.0;
    private static final double FORCE_MULTIPLIER = 750 / MAX_DRAG_DISTANCE;

    private Entity selectedToken;
    private javafx.scene.shape.Line dragLine;

    private Text turnText, forceText;
    private Text scoreBlueText, scoreRedText;
    private boolean isBlueTurn = true;
    private boolean canMove = true;
    private int scoreBlue = 0, scoreRed = 0;
    private boolean isGoalCelebration = false;
    private ImageView goalImageView;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Footy Clash Project - Token Drag Test");
        settings.setWidth(1100);
        settings.setHeight(600);
    }

    @Override
    protected void initGame() {
        getPhysicsWorld().setGravity(0, 0);

        createPitchWalls();

        // Spawn Teams
        for (int i = 0; i < 5; i++) {
            Token.createToken(250, 120 + i * 90, Color.BLUE, EntityType.TEAM_BLUE);
            Token.createToken(810, 120 + i * 90, Color.RED, EntityType.TEAM_RED);
        }

        // Spawn Ball
        Ball.createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

        // Visual Indicator (Arrow)
        dragLine = new javafx.scene.shape.Line();
        dragLine.setStroke(Color.YELLOW);
        dragLine.setStrokeWidth(8);
        dragLine.setOpacity(0.7);
        dragLine.setVisible(false);
        addUINode(dragLine);
    }

    private void createPitchWalls() {
        Walls.createWall(60, 60, 980, 10); // Top
        Walls.createWall(60, 530, 980, 10); // Bottom
        Walls.createWall(67, 50, 10, 180); // Left top
        Walls.createWall(67, 370, 10, 180); // Left bottom
        Walls.createWall(1023, 50, 10, 180); // Right top
        Walls.createWall(1023, 370, 10, 180);// Right bottom

        // Left Goal
        Walls.createWall(10, 0, 10, 1000);

        // Right Goal
        Walls.createWall(1080, 0, 10, 1000);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Drag") {
            @Override
            protected void onActionBegin() {
                if (!canMove)
                    return;

                Point2D mouse = getInput().getMousePositionWorld();
                EntityType team = isBlueTurn ? EntityType.TEAM_BLUE : EntityType.TEAM_RED;

                getGameWorld().getEntitiesByType(team).stream()
                        .filter(e -> e.getCenter().distance(mouse) < 30)
                        .findFirst()
                        .ifPresent(e -> {
                            selectedToken = e;
                            dragLine.setVisible(true);
                        });
            }

            @Override
            protected void onAction() {
                if (selectedToken == null)
                    return;

                Point2D mouse = getInput().getMousePositionWorld();
                Point2D center = selectedToken.getCenter();
                Point2D dragVector = center.subtract(mouse); // Reversed for pull-back logic

                double dist = dragVector.magnitude();
                double limitedDist = Math.min(dist, MAX_DRAG_DISTANCE);
                Point2D direction = dragVector.normalize();

                dragLine.setStartX(center.getX());
                dragLine.setStartY(center.getY());
                dragLine.setEndX(center.getX() + direction.getX() * limitedDist);
                dragLine.setEndY(center.getY() + direction.getY() * limitedDist);

                double force = limitedDist * FORCE_MULTIPLIER;
                forceText.setText(String.format("Force: %.1f N", force));
            }

            @Override
            protected void onActionEnd() {
                if (selectedToken == null)
                    return;

                Point2D dragVector = selectedToken.getCenter().subtract(getInput().getMousePositionWorld());
                double limitedDist = Math.min(dragVector.magnitude(), MAX_DRAG_DISTANCE);

                if (limitedDist > 10) {
                    double force = limitedDist * FORCE_MULTIPLIER;

                    // Call your custom component and pass the calculated vector
                    selectedToken.getComponent(CustomPhysicsComponent.class)
                            .applyImpulse(dragVector.normalize().multiply(force));
                    canMove = false;
                }
                selectedToken = null;
                dragLine.setVisible(false);
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void onUpdate(double tpf) {
        checkCustomCollisions();
        if (!canMove && !isGoalCelebration) {
            // Look for entities holding the CustomPhysicsComponent
            boolean stillMoving = getGameWorld().getEntitiesByComponent(CustomPhysicsComponent.class).stream()
                    .anyMatch(e -> {
                        CustomPhysicsComponent customPhysics = e.getComponent(CustomPhysicsComponent.class);

                        // Pull the velocity vector from your custom formulas
                        double vx = customPhysics.getVelocity().getX();
                        double vy = customPhysics.getVelocity().getY();

                        return (vx * vx + vy * vy) > (MIN_VELOCITY_THRESHOLD * MIN_VELOCITY_THRESHOLD);
                    });

            if (!stillMoving) {
                canMove = true;
                isBlueTurn = !isBlueTurn;
            }
        }
        checkGoals();
        updateUI();
    }

    private void checkGoals() {
        if (isGoalCelebration)
            return;

        getGameWorld().getEntitiesByType(EntityType.BALL).stream().findFirst().ifPresent(ball -> {
            // Ball's center x coordinate.
            // Left goal line is roughly at x=60, Right goal line is around x=1040
            if (ball.getX() < 45) { // Passed the left line entirely
                scoreRed++;
                handleGoal(true); // Blue kicks off after Red scores
            } else if (ball.getX() > 1030) { // Passed the right line entirely
                scoreBlue++;
                handleGoal(false); // Red kicks off after Blue scores
            }
        });
    }

    private void handleGoal(boolean nextTurnBlue) {
        isGoalCelebration = true;
        canMove = false;
        goalImageView.setVisible(true);

        getGameTimer().runOnceAfter(() -> {
            goalImageView.setVisible(false);
            resetPitch(nextTurnBlue);
            isGoalCelebration = false;
        }, javafx.util.Duration.seconds(4));
    }

    private void resetPitch(boolean nextTurnBlue) {
        // Remove all physics entities
        getGameWorld().getEntitiesByType(EntityType.BALL).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(EntityType.TEAM_BLUE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(EntityType.TEAM_RED).forEach(Entity::removeFromWorld);

        // Respawn teams
        for (int i = 0; i < 5; i++) {
            Token.createToken(250, 120 + i * 90, Color.BLUE, EntityType.TEAM_BLUE);
            Token.createToken(810, 120 + i * 90, Color.RED, EntityType.TEAM_RED);
        }

        // Respawn ball
        Ball.createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

        // Reset turn state
        canMove = true;
        isBlueTurn = nextTurnBlue;
    }

    @Override
    protected void initUI() {
        getGameScene().addGameView(new com.almasb.fxgl.app.scene.GameView(new Pitch().getBg(), -1));

        try {
            Image goalImage = new Image(getClass().getResource("/assets/textures/goal.png").toExternalForm());
            goalImageView = new ImageView(goalImage);
        } catch (Exception e) {
            goalImageView = new ImageView();
            System.err.println("Place your goal.png image in src/main/resources/assets/textures/goal.png");
        }
        goalImageView.setFitWidth(500);
        goalImageView.setFitHeight(250);
        goalImageView.setTranslateX((1100 - 500) / 2.0);
        goalImageView.setTranslateY((600 - 250) / 2.0);
        goalImageView.setVisible(false);

        turnText = new Text();
        turnText.setFont(Font.font("Verdana", 24));
        turnText.setTranslateX((1100 - 150) / 2);
        turnText.setTranslateY(90); // Moved down slightly below scoreboard

        // --- NEW SCOREBOARD ---
        javafx.scene.layout.HBox scoreboardBox = new javafx.scene.layout.HBox();
        scoreboardBox.setAlignment(javafx.geometry.Pos.CENTER);
        scoreboardBox.setTranslateX((1100 - 500) / 2.0); // Center standard 500 width
        scoreboardBox.setTranslateY(-5); // Moved higher so it doesn't touch the top pitch wall

        javafx.scene.layout.HBox leftTeamBox = new javafx.scene.layout.HBox(20);
        leftTeamBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        leftTeamBox.setPrefSize(250, 50);
        leftTeamBox.setStyle("-fx-background-color: blue; -fx-background-radius: 25 0 0 25;");

        Text leftName = new Text("BLUE");
        leftName.setFont(Font.font("Verdana", javafx.scene.text.FontWeight.BOLD, 22));
        leftName.setFill(Color.WHITE);

        scoreBlueText = new Text("0");
        scoreBlueText.setFont(Font.font("Verdana", javafx.scene.text.FontWeight.BOLD, 32));
        scoreBlueText.setFill(Color.WHITE);

        leftTeamBox.getChildren().addAll(leftName, scoreBlueText);
        leftTeamBox.setPadding(new javafx.geometry.Insets(0, 40, 0, 0));

        javafx.scene.layout.HBox rightTeamBox = new javafx.scene.layout.HBox(20);
        rightTeamBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        rightTeamBox.setPrefSize(250, 50);
        rightTeamBox.setStyle("-fx-background-color: red; -fx-background-radius: 0 25 25 0;");

        scoreRedText = new Text("0");
        scoreRedText.setFont(Font.font("Verdana", javafx.scene.text.FontWeight.BOLD, 32));
        scoreRedText.setFill(Color.WHITE);

        Text rightName = new Text("RED");
        rightName.setFont(Font.font("Verdana", javafx.scene.text.FontWeight.BOLD, 22));
        rightName.setFill(Color.WHITE);

        rightTeamBox.getChildren().addAll(scoreRedText, rightName);
        rightTeamBox.setPadding(new javafx.geometry.Insets(0, 0, 0, 40));

        scoreboardBox.getChildren().addAll(leftTeamBox, rightTeamBox);

        // Add a subtle drop shadow to make it pop
        scoreboardBox.setEffect(new javafx.scene.effect.DropShadow(5, Color.color(0, 0, 0, 0.3)));
        // ----------------------

        forceText = new Text("READY");
        forceText.setFont(Font.font("Verdana", 18));
        forceText.setTranslateX(500);
        forceText.setTranslateY(580);

        addUINode(scoreboardBox);
        addUINode(turnText);
        addUINode(forceText);
        addUINode(goalImageView);
    }

    private void updateUI() {
        turnText.setText(canMove ? (isBlueTurn ? "BLUE TURN" : "RED TURN") : "MOVING...");
        turnText.setFill(isBlueTurn ? Color.BLUE : Color.RED);
        scoreBlueText.setText(String.valueOf(scoreBlue));
        scoreRedText.setText(String.valueOf(scoreRed));
    }

    // --- CUSTOM 2D ELASTIC COLLISION ENGINE ---
    private void checkCustomCollisions() {
        var entities = getGameWorld().getEntitiesByComponent(CustomPhysicsComponent.class);

        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity e1 = entities.get(i);
                Entity e2 = entities.get(j);

                Point2D p1 = e1.getCenter();
                Point2D p2 = e2.getCenter();

                // 1. Get hitboxes
                double r1 = e1.getType() == EntityType.BALL ? 16.0 : 24.0;
                double r2 = e2.getType() == EntityType.BALL ? 16.0 : 24.0;
                double minDistance = r1 + r2;

                double distance = p1.distance(p2);

                // Prevent zero-distance errors
                if (distance < minDistance && distance > 0.0001) {

                    // 2. Define Masses (Tokens are Heavy, Ball is Light)
                    double m1 = e1.getType() == EntityType.BALL ? 1.0 : 5.0;
                    double m2 = e2.getType() == EntityType.BALL ? 1.0 : 5.0;

                    // In physics engines, we use Inverse Mass for calculations
                    double invM1 = 1.0 / m1;
                    double invM2 = 1.0 / m2;
                    double totalInvMass = invM1 + invM2;

                    Point2D normal = p2.subtract(p1).normalize();

                    // ==========================================
                    // STEP 3: POSITIONAL CORRECTION (Fixes the Clumping)
                    // ==========================================
                    // Separate them immediately before doing velocity math.
                    // Heavy objects move less, light objects move more!
                    double overlap = minDistance - distance;
                    Point2D correction = normal.multiply(overlap / totalInvMass);

                    e1.translate(correction.multiply(-invM1));
                    e2.translate(correction.multiply(invM2));

                    // ==========================================
                    // STEP 4: IMPULSE MATH (Fixes the Ball not moving)
                    // ==========================================
                    CustomPhysicsComponent phys1 = e1.getComponent(CustomPhysicsComponent.class);
                    CustomPhysicsComponent phys2 = e2.getComponent(CustomPhysicsComponent.class);

                    Point2D v1 = phys1.getVelocity();
                    Point2D v2 = phys2.getVelocity();
                    Point2D relativeVelocity = v1.subtract(v2);

                    double speedOnNormal = relativeVelocity.dotProduct(normal);

                    // If they are already moving apart, skip the bounce!
                    if (speedOnNormal > 0)
                        continue;

                    double restitution = 0.8; // Bounciness

                    // The True Elastic Impulse Formula incorporating Mass!
                    double impulseScalar = -(1 + restitution) * speedOnNormal;
                    impulseScalar /= totalInvMass;

                    Point2D impulseVector = normal.multiply(impulseScalar);

                    // Apply the new velocities based on mass
                    phys1.setVelocity(v1.add(impulseVector.multiply(invM1)));
                    phys2.setVelocity(v2.subtract(impulseVector.multiply(invM2)));
                }
            }
        }
    }

    public static void main(String[] args) {
        launchArgs = args;
        launch(args);
    }
}
