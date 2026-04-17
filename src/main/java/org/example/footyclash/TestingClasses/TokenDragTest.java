package org.example.footyclash.TestingClasses;

import GameClasses.Ball;
import GameClasses.Pitch;
import GameClasses.Token;
import GameClasses.Walls;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import GameClasses.CustomPhysicsComponent;

import static com.almasb.fxgl.dsl.FXGL.*;

public class TokenDragTest extends GameApplication {

    public enum EntityType {
        TEAM_BLUE, TEAM_RED, BALL, WALL
    }

    private static final double MIN_VELOCITY_THRESHOLD = 100.0;
    private static final double MAX_DRAG_DISTANCE = 150.0;

    // Reduced force multiplier to keep the speed manageable
    private static final double FORCE_MULTIPLIER = 30.0;

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
        // FXGL physics (Box2D) removed! Everything uses CustomPhysicsComponent and math.
        
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
                Point2D dragVector = center.subtract(mouse);

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
            boolean stillMoving = getGameWorld().getEntitiesByComponent(CustomPhysicsComponent.class).stream()
                    .anyMatch(e -> {
                        CustomPhysicsComponent customPhysics = e.getComponent(CustomPhysicsComponent.class);

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
            if (ball.getX() < 45) {
                scoreRed++;
                handleGoal(true);
            } else if (ball.getX() > 1030) {
                scoreBlue++;
                handleGoal(false);
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
        getGameWorld().getEntitiesByType(EntityType.BALL).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(EntityType.TEAM_BLUE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(EntityType.TEAM_RED).forEach(Entity::removeFromWorld);

        for (int i = 0; i < 5; i++) {
            Token.createToken(250, 120 + i * 90, Color.BLUE, EntityType.TEAM_BLUE);
            Token.createToken(810, 120 + i * 90, Color.RED, EntityType.TEAM_RED);
        }

        Ball.createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

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
        turnText.setTranslateY(90);

        javafx.scene.layout.HBox scoreboardBox = new javafx.scene.layout.HBox();
        scoreboardBox.setAlignment(javafx.geometry.Pos.CENTER);
        scoreboardBox.setTranslateX((1100 - 500) / 2.0);
        scoreboardBox.setTranslateY(-5);

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
        scoreboardBox.setEffect(new javafx.scene.effect.DropShadow(5, Color.color(0, 0, 0, 0.3)));

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

                double r1 = e1.getType() == EntityType.BALL ? 16.0 : 24.0;
                double r2 = e2.getType() == EntityType.BALL ? 16.0 : 24.0;
                double minDistance = r1 + r2;

                double distance = p1.distance(p2);

                if (distance < minDistance && distance > 0.0001) {

                    CustomPhysicsComponent phys1 = e1.getComponent(CustomPhysicsComponent.class);
                    CustomPhysicsComponent phys2 = e2.getComponent(CustomPhysicsComponent.class);

                    double m1 = phys1.getMass();
                    double m2 = phys2.getMass();

                    double invM1 = 1.0 / m1;
                    double invM2 = 1.0 / m2;
                    double totalInvMass = invM1 + invM2;

                    Point2D normal = p2.subtract(p1).normalize();

                    double overlap = minDistance - distance;
                    Point2D correction = normal.multiply(overlap / totalInvMass);

                    e1.translate(correction.multiply(-invM1));
                    e2.translate(correction.multiply(invM2));

                    Point2D v1 = phys1.getVelocity();
                    Point2D v2 = phys2.getVelocity();
                    Point2D relativeVelocity = v1.subtract(v2);

                    double speedOnNormal = relativeVelocity.dotProduct(normal);

                    if (speedOnNormal < 0) {
                        continue;
                    }

                    double restitution = Math.min(phys1.getRestitution(), phys2.getRestitution());

                    double impulseScalar = -(1 + restitution) * speedOnNormal;
                    impulseScalar /= totalInvMass;

                    Point2D impulseVector = normal.multiply(impulseScalar);

                    phys1.setVelocity(v1.add(impulseVector.multiply(invM1)));
                    phys2.setVelocity(v2.subtract(impulseVector.multiply(invM2)));
                }
            }
        }

        // --- Custom Token vs Wall Collisions ---
        var walls = getGameWorld().getEntitiesByType(EntityType.WALL);
        for (Entity e : entities) {
            CustomPhysicsComponent phys = e.getComponent(CustomPhysicsComponent.class);
            Point2D pos = e.getCenter();
            double radius = e.getType() == EntityType.BALL ? 16.0 : 24.0;

            for (Entity wall : walls) {
                double wx = wall.getX();
                double wy = wall.getY();
                double ww = wall.getWidth();
                double wh = wall.getHeight();

                // Find nearest point on the wall rectangle to the token's center
                double nearestX = Math.max(wx, Math.min(pos.getX(), wx + ww));
                double nearestY = Math.max(wy, Math.min(pos.getY(), wy + wh));

                double dx = pos.getX() - nearestX;
                double dy = pos.getY() - nearestY;
                double distanceSq = dx * dx + dy * dy;

                if (distanceSq < radius * radius && distanceSq > 0.0001) {
                    double distance = Math.sqrt(distanceSq);

                    double normalX = dx / distance;
                    double normalY = dy / distance;

                    // Depenetrate: push token completely out of the wall
                    double overlap = radius - distance;
                    e.translate(normalX * overlap, normalY * overlap);

                    Point2D vel = phys.getVelocity();
                    double dotProduct = vel.getX() * normalX + vel.getY() * normalY;

                    // Apply reflection only if the token is moving towards the wall
                    if (dotProduct < 0) {
                        double wallRestitution = 0.8; // Bounce factor off the wall
                        double restitution = phys.getRestitution() * wallRestitution;
                        double scalar = -(1 + restitution) * dotProduct;
                        phys.setVelocity(vel.add(normalX * scalar, normalY * scalar));
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}