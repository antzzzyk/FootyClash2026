package org.example.footyclash.TestingClasses;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class TokenDragTest extends GameApplication {

    public enum EntityType {
        TEAM_BLUE, TEAM_RED, BALL, WALL
    }

    // --- Physics Constants ---
    private static final float TOKEN_DENSITY = 1.0f;
    private static final float BALL_DENSITY = 0.2f;
    private static final float RESTITUTION = 0.7f;
    private static final float FRICTION = 0.1f;
    private static final float DAMPING = 0.6f;

    private static final double FORCE_MULTIPLIER = 0.75;
    private static final double MIN_VELOCITY_THRESHOLD = 20.0;
    private static final double MAX_DRAG_DISTANCE = 150.0;

    private Entity selectedToken;
    private Rectangle arrowBody;

    private Text turnText, forceText, scoreText;
    private boolean isBlueTurn = true;
    private boolean canMove = true;
    private int scoreBlue = 0, scoreRed = 0;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Footy Clash - Token Drag Test");
        settings.setWidth(1100);
        settings.setHeight(600);
    }

    @Override
    protected void initGame() {
        getPhysicsWorld().setGravity(0, 0);

        createPitchWalls();

        // Spawn Teams
        for (int i = 0; i < 5; i++) {
            createToken(250, 120 + i * 90, Color.BLUE, EntityType.TEAM_BLUE);
            createToken(810, 120 + i * 90, Color.RED, EntityType.TEAM_RED);
        }

        // Spawn Ball
        createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

        // Visual Indicator (Arrow)
        arrowBody = new Rectangle(0, 8, Color.YELLOW);
        arrowBody.setArcWidth(5);
        arrowBody.setArcHeight(5);
        arrowBody.setOpacity(0.7);
        arrowBody.setVisible(false);
        addUINode(arrowBody);
    }

    private void createToken(double x, double y, Color color, EntityType type) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().density(TOKEN_DENSITY).restitution(RESTITUTION).friction(FRICTION));

        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setLinearDamping(DAMPING);
            physics.getBody().setSleepingAllowed(true);
        });

        entityBuilder()
                .type(type)
                .at(x, y)
                .viewWithBBox(new Circle(20, 20, 20, color))
                .with(physics)
                .collidable()
                .buildAndAttach();
    }

    private void createBall(double x, double y) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().density(BALL_DENSITY).restitution(0.9f).friction(0.01f));

        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setLinearDamping(DAMPING * 0.7f);
            physics.getBody().setSleepingAllowed(true);
        });

        entityBuilder()
                .type(EntityType.BALL)
                .at(x, y)
                .viewWithBBox(new Circle(12, 12, 12, Color.BLACK))
                .with(physics)
                .collidable()
                .buildAndAttach();
    }

    private void createPitchWalls() {
        createWall(60, 40, 980, 10); // Top
        createWall(60, 550, 980, 10); // Bottom
        createWall(60, 50, 10, 180); // Left top
        createWall(60, 370, 10, 180); // Left bottom
        createWall(1030, 50, 10, 180); // Right top
        createWall(1030, 370, 10, 180);// Right bottom
    }

    private void createWall(double x, double y, double w, double h) {
        entityBuilder()
                .type(EntityType.WALL)
                .at(x, y)
                .viewWithBBox(new Rectangle(w, h, Color.TRANSPARENT))
                .with(new PhysicsComponent())
                .buildAndAttach();
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
                            arrowBody.setVisible(true);
                        });
            }

            @Override
            protected void onAction() {
                if (selectedToken == null)
                    return;

                Point2D mouse = getInput().getMousePositionWorld();
                Point2D center = selectedToken.getCenter();
                double dist = mouse.distance(center);

                arrowBody.setWidth(Math.min(dist, MAX_DRAG_DISTANCE));
                double angle = Math.toDegrees(Math.atan2(mouse.getY() - center.getY(), mouse.getX() - center.getX()));
                arrowBody.setRotate(angle);

                arrowBody.setTranslateX(center.getX());
                arrowBody.setTranslateY(center.getY() - arrowBody.getHeight() / 2);

                forceText.setText(String.format("Power: %.0f%%", Math.min((dist / MAX_DRAG_DISTANCE) * 100, 100)));
            }

            @Override
            protected void onActionEnd() {
                if (selectedToken == null)
                    return;

                Point2D dragVector = getInput().getMousePositionWorld().subtract(selectedToken.getCenter());

                if (dragVector.magnitude() > 10) {
                    double force = dragVector.magnitude() * FORCE_MULTIPLIER;
                    selectedToken.getComponent(PhysicsComponent.class)
                            .applyLinearImpulse(dragVector.normalize().multiply(force), selectedToken.getCenter(),
                                    true);
                    canMove = false;
                }

                selectedToken = null;
                arrowBody.setVisible(false);
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!canMove) {
            boolean stillMoving = getGameWorld().getEntitiesByComponent(PhysicsComponent.class).stream()
                    .anyMatch(e -> {
                        PhysicsComponent physics = e.getComponent(PhysicsComponent.class);
                        double vx = physics.getVelocityX();
                        double vy = physics.getVelocityY();
                        // Using square of magnitude for performance
                        return (vx * vx + vy * vy) > (MIN_VELOCITY_THRESHOLD * MIN_VELOCITY_THRESHOLD);
                    });

            if (!stillMoving) {
                canMove = true;
                isBlueTurn = !isBlueTurn;
            }
        }
        updateUI();
    }

    @Override
    protected void initUI() {
        Image image = new Image(
                getClass().getResource("/assets/textures/match.png").toExternalForm());
        ImageView bg = new ImageView(image);
        bg.setFitWidth(1100);
        bg.setFitHeight(600);
        getGameScene().addGameView(new com.almasb.fxgl.app.scene.GameView(bg, -1));

        turnText = new Text();
        turnText.setFont(Font.font("Verdana", 24));
        turnText.setTranslateX(440);
        turnText.setTranslateY(40);

        scoreText = new Text("BLUE: 0 | RED: 0");
        scoreText.setFont(Font.font("Verdana", 20));
        scoreText.setTranslateX(460);
        scoreText.setTranslateY(75);

        forceText = new Text("READY");
        forceText.setFont(Font.font("Verdana", 18));
        forceText.setTranslateX(500);
        forceText.setTranslateY(580);

        addUINode(turnText);
        addUINode(scoreText);
        addUINode(forceText);
    }

    private void updateUI() {
        turnText.setText(canMove ? (isBlueTurn ? "BLUE TURN" : "RED TURN") : "MOVING...");
        turnText.setFill(isBlueTurn ? Color.BLUE : Color.RED);
        scoreText.setText("BLUE: " + scoreBlue + " | RED: " + scoreRed);
    }

    public static void main(String[] args) {
        launch(args);
    }
}