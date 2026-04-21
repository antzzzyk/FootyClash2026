package org.example.footyclash.TestingClasses;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
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
    private static final float FRICTION = 0.05f;
    private static final float DAMPING = 0.8f;
    private static final double FORCE_MULTIPLIER = 3.5;
    private static final double MIN_VELOCITY_THRESHOLD = 20.0;
    private static final double MAX_DRAG_DISTANCE = 165.0;

    // --- Game Variables ---
    private Entity selectedToken;
    private javafx.scene.shape.Line dragLine;
    private Text turnText, forceText, scoreBlueText, scoreRedText;
    private boolean isBlueTurn = true;
    private boolean canMove = true;
    private int scoreBlue = 0, scoreRed = 0;
    private boolean isGoalCelebration = false;
    private ImageView goalImageView;

    // --- THESE ARE THE VARIABLES YOUR TEAMMATE ASKED FOR ---
    private String p1ChosenSkin = "plain.png";
    private String p2ChosenSkin = "goal.png";

    private String[] skins = { "plain.png", "goal.png" };

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Footy Clash - Full Game Flow");
        settings.setWidth(1100);
        settings.setHeight(600);
    }

    @Override
    protected void initGame() {
        // Empty on purpose! Waits for the selection screen to finish.
    }

    @Override
    protected void initUI() {
        showTokenSelectionScreen();
    }

    private void showTokenSelectionScreen() {
        // Background
        Image image = new Image(getClass().getResource("/assets/textures/tokenselect.png").toExternalForm());
        ImageView bg = new ImageView(image);
        bg.setFitWidth(getAppWidth());
        bg.setFitHeight(getAppHeight());
        getGameScene().addUINode(bg);

        int[] p1SkinIndex = { 0 };
        int[] p2SkinIndex = { 0 };

        // --- PLAYER 1 SETUP ---
        Texture p1Token = texture(skins[p1SkinIndex[0]]);
        p1Token.setFitWidth(150); p1Token.setFitHeight(150);
        p1Token.setClip(new Circle(75, 75, 75)); // <-- ROUND CLIP ADDED HERE
        p1Token.setTranslateX(200); p1Token.setTranslateY(225);

        Button p1Next = new Button(">");
        p1Next.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        p1Next.setTranslateX(290); p1Next.setTranslateY(260);

        p1Next.setOnAction(e -> {
            p1SkinIndex[0] = (p1SkinIndex[0] + 1) % skins.length;
            p1Token.setImage(texture(skins[p1SkinIndex[0]]).getImage());
        });

        // --- PLAYER 2 SETUP ---
        Texture p2Token = texture(skins[p2SkinIndex[0]]);
        p2Token.setFitWidth(150); p2Token.setFitHeight(150);
        p2Token.setClip(new Circle(75, 75, 75)); // <-- ROUND CLIP ADDED HERE
        p2Token.setTranslateX(750); p2Token.setTranslateY(225);

        Button p2Next = new Button(">");
        p2Next.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        p2Next.setTranslateX(965); p2Next.setTranslateY(260);

        p2Next.setOnAction(e -> {
            p2SkinIndex[0] = (p2SkinIndex[0] + 1) % skins.length;
            p2Token.setImage(texture(skins[p2SkinIndex[0]]).getImage());
        });

        // --- THE "START MATCH" BUTTON ---
        Button startButton = new Button("START MATCH");
        startButton.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color: #00FF00; -fx-text-fill: black;");
        startButton.setPrefSize(200, 60);
        startButton.setTranslateX((getAppWidth() - 200) / 2.0);
        startButton.setTranslateY(450);

        startButton.setOnAction(e -> {
            // Save the choices
            p1ChosenSkin = skins[p1SkinIndex[0]];
            p2ChosenSkin = skins[p2SkinIndex[0]];

            // Clear the selection UI
            getGameScene().clearUINodes();

            // Start the actual game
            startActualMatch();
        });

        getGameScene().addUINodes(p1Token, p1Next, p2Token, p2Next, startButton);
    }

    private void startActualMatch() {
        getPhysicsWorld().setGravity(0, 0);
        createPitchWalls();

        // Spawn teams with their custom images
        for (int i = 0; i < 5; i++) {
            createToken(250, 120 + i * 90, p1ChosenSkin, EntityType.TEAM_BLUE);
            createToken(810, 120 + i * 90, p2ChosenSkin, EntityType.TEAM_RED);
        }

        createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

        dragLine = new javafx.scene.shape.Line();
        dragLine.setStroke(Color.YELLOW);
        dragLine.setStrokeWidth(8);
        dragLine.setOpacity(0.7);
        dragLine.setVisible(false);
        addUINode(dragLine);

        setupMatchUI();
    }

    private void createToken(double x, double y, String textureName, EntityType type) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().density(TOKEN_DENSITY).restitution(RESTITUTION).friction(FRICTION));

        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setLinearDamping(DAMPING);
            physics.getBody().setAngularDamping(2.0f);
            physics.getBody().setSleepingAllowed(true);
        });

        Texture tokenTexture = texture(textureName);
        tokenTexture.setFitWidth(48);
        tokenTexture.setFitHeight(48);
        tokenTexture.setClip(new Circle(24, 24, 24)); // <-- ROUND CLIP ADDED HERE FOR THE MATCH!

        entityBuilder()
                .type(type)
                .at(x, y)
                .bbox(new HitBox(BoundingShape.circle(24)))
                .view(tokenTexture)
                .with(physics)
                .collidable()
                .buildAndAttach();
    }

    private void createBall(double x, double y) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setFixtureDef(new FixtureDef().density(BALL_DENSITY).restitution(0.9f).friction(0.01f));

        physics.setOnPhysicsInitialized(() -> {
            physics.getBody().setLinearDamping(DAMPING * 1.5f);
            physics.getBody().setAngularDamping(1.5f);
            physics.getBody().setSleepingAllowed(true);
        });

        Circle ballShape = new Circle(16, 16, 16, Color.WHITE);
        ballShape.setStroke(Color.BLACK);
        ballShape.setStrokeWidth(3);

        entityBuilder()
                .type(EntityType.BALL)
                .at(x, y)
                .viewWithBBox(ballShape)
                .with(physics)
                .collidable()
                .buildAndAttach();
    }

    private void createPitchWalls() {
        createWall(60, 60, 980, 10);
        createWall(60, 530, 980, 10);
        createWall(67, 50, 10, 180);
        createWall(67, 370, 10, 180);
        createWall(1023, 50, 10, 180);
        createWall(1023, 370, 10, 180);
        createWall(10, 0, 10, 1000);
        createWall(1080, 0, 10, 1000);
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
                if (!canMove) return;

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
                if (selectedToken == null) return;

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
                if(forceText != null) forceText.setText(String.format("Force: %.1f N", force));
            }

            @Override
            protected void onActionEnd() {
                if (selectedToken == null) return;

                Point2D dragVector = selectedToken.getCenter().subtract(getInput().getMousePositionWorld());
                double limitedDist = Math.min(dragVector.magnitude(), MAX_DRAG_DISTANCE);

                if (limitedDist > 10) {
                    double force = limitedDist * FORCE_MULTIPLIER;
                    selectedToken.getComponent(PhysicsComponent.class)
                            .applyLinearImpulse(dragVector.normalize().multiply(force), selectedToken.getCenter(), true);
                    canMove = false;
                }

                selectedToken = null;
                dragLine.setVisible(false);
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void onUpdate(double tpf) {
        if (getGameWorld().getEntitiesByType(EntityType.BALL).isEmpty()) return;

        if (!canMove && !isGoalCelebration) {
            boolean stillMoving = getGameWorld().getEntitiesByComponent(PhysicsComponent.class).stream()
                    .anyMatch(e -> {
                        PhysicsComponent physics = e.getComponent(PhysicsComponent.class);
                        double vx = physics.getVelocityX();
                        double vy = physics.getVelocityY();
                        return (vx * vx + vy * vy) > (MIN_VELOCITY_THRESHOLD * MIN_VELOCITY_THRESHOLD);
                    });

            if (!stillMoving) {
                canMove = true;
                isBlueTurn = !isBlueTurn;
            }
        }
        checkGoals();
        updateUIElements();
    }

    private void checkGoals() {
        if (isGoalCelebration) return;

        getGameWorld().getEntitiesByType(EntityType.BALL).stream().findFirst().ifPresent(ball -> {
            if (ball.getX() < 45) {
                scoreRed++;
                handleGoal(true);
            } else if (ball.getX() > 1040) {
                scoreBlue++;
                handleGoal(false);
            }
        });
    }

    private void handleGoal(boolean nextTurnBlue) {
        isGoalCelebration = true;
        canMove = false;
        if(goalImageView != null) goalImageView.setVisible(true);

        getGameTimer().runOnceAfter(() -> {
            if(goalImageView != null) goalImageView.setVisible(false);
            resetPitch(nextTurnBlue);
            isGoalCelebration = false;
        }, javafx.util.Duration.seconds(4));
    }

    private void resetPitch(boolean nextTurnBlue) {
        getGameWorld().getEntitiesByType(EntityType.BALL).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(EntityType.TEAM_BLUE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(EntityType.TEAM_RED).forEach(Entity::removeFromWorld);

        for (int i = 0; i < 5; i++) {
            createToken(250, 120 + i * 90, p1ChosenSkin, EntityType.TEAM_BLUE);
            createToken(810, 120 + i * 90, p2ChosenSkin, EntityType.TEAM_RED);
        }
        createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

        canMove = true;
        isBlueTurn = nextTurnBlue;
    }

    private void setupMatchUI() {
        Image image = new Image(getClass().getResource("/assets/textures/match.png").toExternalForm());
        ImageView bg = new ImageView(image);
        bg.setFitWidth(1100);
        bg.setFitHeight(600);
        getGameScene().addGameView(new com.almasb.fxgl.app.scene.GameView(bg, -1));

        try {
            Image goalImage = new Image(getClass().getResource("/assets/textures/goal.png").toExternalForm());
            goalImageView = new ImageView(goalImage);
        } catch (Exception e) {
            goalImageView = new ImageView();
        }
        goalImageView.setFitWidth(500); goalImageView.setFitHeight(250);
        goalImageView.setTranslateX((1100 - 500) / 2.0); goalImageView.setTranslateY((600 - 250) / 2.0);
        goalImageView.setVisible(false);

        turnText = new Text();
        turnText.setFont(Font.font("Verdana", 24));
        turnText.setTranslateX(440); turnText.setTranslateY(90);

        javafx.scene.layout.HBox scoreboardBox = new javafx.scene.layout.HBox();
        scoreboardBox.setAlignment(javafx.geometry.Pos.CENTER);
        scoreboardBox.setTranslateX((1100 - 500) / 2.0);
        scoreboardBox.setTranslateY(-5);

        javafx.scene.layout.HBox leftTeamBox = new javafx.scene.layout.HBox(20);
        leftTeamBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        leftTeamBox.setPrefSize(250, 50);
        leftTeamBox.setStyle("-fx-background-color: blue; -fx-background-radius: 25 0 0 25;");
        Text leftName = new Text("P1");
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
        Text rightName = new Text("P2");
        rightName.setFont(Font.font("Verdana", javafx.scene.text.FontWeight.BOLD, 22));
        rightName.setFill(Color.WHITE);
        rightTeamBox.getChildren().addAll(scoreRedText, rightName);
        rightTeamBox.setPadding(new javafx.geometry.Insets(0, 0, 0, 40));

        scoreboardBox.getChildren().addAll(leftTeamBox, rightTeamBox);
        scoreboardBox.setEffect(new javafx.scene.effect.DropShadow(5, Color.color(0, 0, 0, 0.3)));

        forceText = new Text("READY");
        forceText.setFont(Font.font("Verdana", 18));
        forceText.setTranslateX(500); forceText.setTranslateY(580);

        addUINode(scoreboardBox);
        addUINode(turnText);
        addUINode(forceText);
        addUINode(goalImageView);
    }

    private void updateUIElements() {
        if(turnText == null) return;
        turnText.setText(canMove ? (isBlueTurn ? "P1 TURN" : "P2 TURN") : "MOVING...");
        turnText.setFill(isBlueTurn ? Color.BLUE : Color.RED);
        scoreBlueText.setText(String.valueOf(scoreBlue));
        scoreRedText.setText(String.valueOf(scoreRed));
    }

    public static void main(String[] args) {
        launch(args);
    }
}