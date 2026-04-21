package org.example.footyclash.TestingClasses;

import GameClasses.Ball;
import GameClasses.Pitch;
import GameClasses.Token;
import GameClasses.Walls;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Pos;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import GameClasses.CustomPhysicsComponent;

import static com.almasb.fxgl.dsl.FXGL.*;

public class Final extends GameApplication {

    public enum GameState {
        MAIN_MENU, PLAYER_SELECT, MATCH, WINNER
    }

    private GameState gameState = GameState.MAIN_MENU;

    // --- Physics & Match Fields ---
    private Color c1 = Color.BLUE, c2 = Color.RED;
    private static final double MIN_VELOCITY_THRESHOLD = 100.0;
    private static final double MAX_DRAG_DISTANCE = 150.0;
    private static final double FORCE_MULTIPLIER = 30.0;

    private Entity selectedToken;
    private javafx.scene.shape.Line dragLine;
    private Text turnText, forceText, vectorText, physicsText;
    private Text scoreBlueText, scoreRedText;
    private boolean isBlueTurn = true;
    private boolean canMove = true;
    private int scoreBlue = 0, scoreRed = 0;
    private boolean isGoalCelebration = false;
    private Text goalText;

    private Entity activeToken;
    private Point2D lastVelocity = Point2D.ZERO;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Footy Clash Final");
        settings.setWidth(1100);
        settings.setHeight(600);
    }

    @Override
    protected void initGame() {
        // Handled dynamically per GameState
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Drag") {
            @Override
            protected void onActionBegin() {
                if (gameState != GameState.MATCH || !canMove)
                    return;

                Point2D mouse = getInput().getMousePositionWorld();
                TokenDragTest.EntityType team = isBlueTurn ? TokenDragTest.EntityType.TEAM_BLUE
                        : TokenDragTest.EntityType.TEAM_RED;

                getGameWorld().getEntitiesByType(team).stream()
                        .filter(e -> e.getCenter().distance(mouse) < 30)
                        .findFirst()
                        .ifPresent(e -> {
                            selectedToken = e;
                            activeToken = e;
                            lastVelocity = Point2D.ZERO;
                            dragLine.setVisible(true);
                            resetPhysicsText();
                        });
            }

            @Override
            protected void onAction() {
                if (gameState != GameState.MATCH || selectedToken == null)
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

                double displayX = -direction.getX() * force;
                double displayY = direction.getY() * force;
                double angle = Math.toDegrees(Math.atan2(displayY, displayX));
                if (angle < 0)
                    angle += 360;

                vectorText.setText(String.format("Vector: (%.1f, %.1f)   Angle: %.0f°", displayX, displayY, angle));

                double mass = selectedToken.getComponent(CustomPhysicsComponent.class).getMass();
                double a = force / mass;
                double v = force / mass;
                physicsText.setText(String.format("m: %.1f kg   a: %.1f   v: %.1f", mass, a, v));
            }

            @Override
            protected void onActionEnd() {
                if (gameState != GameState.MATCH || selectedToken == null)
                    return;

                Point2D dragVector = selectedToken.getCenter().subtract(getInput().getMousePositionWorld());
                double limitedDist = Math.min(dragVector.magnitude(), MAX_DRAG_DISTANCE);

                if (limitedDist > 10) {
                    double force = limitedDist * FORCE_MULTIPLIER;

                    CustomPhysicsComponent phys = selectedToken.getComponent(CustomPhysicsComponent.class);
                    phys.applyImpulse(dragVector.normalize().multiply(force));

                    lastVelocity = phys.getVelocity();
                    canMove = false;
                } else {
                    resetPhysicsText();
                }

                selectedToken = null;
                dragLine.setVisible(false);
            }
        }, MouseButton.PRIMARY);
    }

    @Override
    protected void initUI() {
        buildMainMenu();
    }

    // --- PHASE 1: MAIN MENU ---
    private void buildMainMenu() {
        getGameScene().clearUINodes();
        getGameScene().clearGameViews();
        new java.util.ArrayList<>(getGameWorld().getEntities()).forEach(Entity::removeFromWorld);

        Image image = new Image(getClass().getResource("/assets/textures/mainmenu.png").toExternalForm());
        ImageView bg = new ImageView(image);
        bg.setFitWidth(getAppWidth());
        bg.setFitHeight(getAppHeight());
        getGameScene().addUINode(bg);

        VBox mmenu = new VBox(10);

        Button startBtn = new Button("Start");
        startBtn.setPrefSize(250, 80);
        startBtn.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue; -fx-text-fill: white;");

        startBtn.setOnAction(e -> {
            gameState = GameState.PLAYER_SELECT;
            buildPlayerSelectUI();
        });

        mmenu.getChildren().add(startBtn);

        mmenu.setTranslateX(650);
        mmenu.setTranslateY(getAppHeight() / 2.0 - mmenu.getHeight() / 2.0);

        getGameScene().addUINode(mmenu);
    }

    // --- PHASE 2: PLAYER SELECT ---
    private void buildPlayerSelectUI() {
        getGameScene().clearUINodes();

        Image image = new Image(getClass().getResource("/assets/textures/tokenselect.png").toExternalForm());
        ImageView bg = new ImageView(image);
        bg.setFitWidth(getAppWidth());
        bg.setFitHeight(getAppHeight());
        getGameScene().addUINode(bg);

        Color[] colors = {
                Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.ORANGE,
                Color.PURPLE, Color.CYAN, Color.MAGENTA, Color.PINK
        };

        String[] colorNames = {
                "BLUE", "RED", "GREEN", "YELLOW", "ORANGE", "PURPLE", "CYAN", "MAGENTA", "PINK"
        };

        int[] p1ColorIndex = { 0 }; // Default BLUE
        int[] p2ColorIndex = { 1 }; // Default RED

        // Player 1 Setup
        Circle p1Circle = new Circle(75, 75, 75);
        p1Circle.setFill(colors[p1ColorIndex[0]]);
        p1Circle.setStroke(Color.BLACK);
        p1Circle.setStrokeWidth(3);
        p1Circle.setTranslateX(140);
        p1Circle.setTranslateY(225);

        Label p1ColorText = new Label(colorNames[p1ColorIndex[0]]);
        p1ColorText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        p1ColorText.setPrefWidth(150);
        p1ColorText.setAlignment(Pos.CENTER);
        p1ColorText.setTranslateX(140);
        p1ColorText.setTranslateY(150);

        Button p1Prev = new Button("<");
        p1Prev.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-cursor: hand;");
        p1Prev.setTranslateX(65);
        p1Prev.setTranslateY(260);

        Button p1Next = new Button(">");
        p1Next.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-cursor: hand;");
        p1Next.setTranslateX(290);
        p1Next.setTranslateY(260);

        p1Prev.setOnAction(e -> {
            do {
                p1ColorIndex[0] = (p1ColorIndex[0] - 1 + colors.length) % colors.length;
            } while (p1ColorIndex[0] == p2ColorIndex[0]);
            p1Circle.setFill(colors[p1ColorIndex[0]]);
            p1ColorText.setText(colorNames[p1ColorIndex[0]]);
        });

        p1Next.setOnAction(e -> {
            do {
                p1ColorIndex[0] = (p1ColorIndex[0] + 1) % colors.length;
            } while (p1ColorIndex[0] == p2ColorIndex[0]);
            p1Circle.setFill(colors[p1ColorIndex[0]]);
            p1ColorText.setText(colorNames[p1ColorIndex[0]]);
        });

        // Player 2 Setup
        Circle p2Circle = new Circle(75, 75, 75);
        p2Circle.setFill(colors[p2ColorIndex[0]]);
        p2Circle.setStroke(Color.BLACK);
        p2Circle.setStrokeWidth(3);
        p2Circle.setTranslateX(825);
        p2Circle.setTranslateY(225);

        Label p2ColorText = new Label(colorNames[p2ColorIndex[0]]);
        p2ColorText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        p2ColorText.setPrefWidth(150);
        p2ColorText.setAlignment(Pos.CENTER);
        p2ColorText.setTranslateX(825);
        p2ColorText.setTranslateY(150);

        Button p2Prev = new Button("<");
        p2Prev.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-cursor: hand;");
        p2Prev.setTranslateX(740);
        p2Prev.setTranslateY(260);

        Button p2Next = new Button(">");
        p2Next.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-cursor: hand;");
        p2Next.setTranslateX(965);
        p2Next.setTranslateY(260);

        p2Prev.setOnAction(e -> {
            do {
                p2ColorIndex[0] = (p2ColorIndex[0] - 1 + colors.length) % colors.length;
            } while (p2ColorIndex[0] == p1ColorIndex[0]);
            p2Circle.setFill(colors[p2ColorIndex[0]]);
            p2ColorText.setText(colorNames[p2ColorIndex[0]]);
        });

        p2Next.setOnAction(e -> {
            do {
                p2ColorIndex[0] = (p2ColorIndex[0] + 1) % colors.length;
            } while (p2ColorIndex[0] == p1ColorIndex[0]);
            p2Circle.setFill(colors[p2ColorIndex[0]]);
            p2ColorText.setText(colorNames[p2ColorIndex[0]]);
        });

        Button startButton = new Button("START MATCH");
        startButton.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color: #00FF00; -fx-text-fill: black;");
        startButton.setPrefSize(250, 60);
        startButton.setTranslateX((getAppWidth() - 250) / 2.0);
        startButton.setTranslateY(450);

        startButton.setOnAction(e -> {
            c1 = colors[p1ColorIndex[0]];
            c2 = colors[p2ColorIndex[0]];

            gameState = GameState.MATCH;
            buildMatch();
        });

        getGameScene().addUINodes(p1ColorText, p1Circle, p1Prev, p1Next,
                p2ColorText, p2Circle, p2Prev, p2Next, startButton);
    }

    // --- PHASE 3: MATCH ---
    private void buildMatch() {
        getGameScene().clearUINodes();
        new java.util.ArrayList<>(getGameWorld().getEntities()).forEach(Entity::removeFromWorld);

        getGameScene().addGameView(new com.almasb.fxgl.app.scene.GameView(new Pitch().getBg(), -1));

        createPitchWalls();

        for (int i = 0; i < 5; i++) {
            Token.createToken(250, 120 + i * 90, c1, TokenDragTest.EntityType.TEAM_BLUE);
            Token.createToken(810, 120 + i * 90, c2, TokenDragTest.EntityType.TEAM_RED);
        }

        Ball.createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

        dragLine = new javafx.scene.shape.Line();
        dragLine.setStroke(Color.YELLOW);
        dragLine.setStrokeWidth(8);
        dragLine.setOpacity(0.7);
        dragLine.setVisible(false);
        addUINode(dragLine);

        buildMatchUI();
    }

    private void buildMatchUI() {
        goalText = new Text("GOAL!!!");
        goalText.setFont(Font.font("Verdana", javafx.scene.text.FontWeight.BOLD, 100));
        goalText.setFill(Color.YELLOW);
        goalText.setStroke(Color.BLACK);
        goalText.setStrokeWidth(4);
        goalText.setEffect(new javafx.scene.effect.DropShadow(10, Color.BLACK));
        goalText.setTranslateX(1100 / 2.0 - 230);
        goalText.setTranslateY(700);
        goalText.setVisible(false);

        turnText = new Text();
        turnText.setFont(Font.font("Verdana", 24));
        turnText.setTranslateX(920);
        turnText.setTranslateY(40);

        javafx.scene.layout.HBox scoreboardBox = new javafx.scene.layout.HBox();
        scoreboardBox.setAlignment(javafx.geometry.Pos.CENTER);
        scoreboardBox.setTranslateX((1100 - 500) / 2.0);
        scoreboardBox.setTranslateY(-5);

        javafx.scene.layout.HBox leftTeamBox = new javafx.scene.layout.HBox(20);
        leftTeamBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        leftTeamBox.setPrefSize(250, 50);
        String styleC1 = String.format("-fx-background-color: #%02X%02X%02X; -fx-background-radius: 25 0 0 25;",
                (int) (c1.getRed() * 255), (int) (c1.getGreen() * 255), (int) (c1.getBlue() * 255));
        leftTeamBox.setStyle(styleC1);

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
        String styleC2 = String.format("-fx-background-color: #%02X%02X%02X; -fx-background-radius: 0 25 25 0;",
                (int) (c2.getRed() * 255), (int) (c2.getGreen() * 255), (int) (c2.getBlue() * 255));
        rightTeamBox.setStyle(styleC2);

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

        forceText = new Text("Force: 0.0 N");
        forceText.setFont(Font.font("Verdana", 18));

        vectorText = new Text("Vector: (0.0, 0.0)   Angle: 0°");
        vectorText.setFont(Font.font("Verdana", 18));

        physicsText = new Text("m: 0.0 kg   a: 0.0   v: 0.0");
        physicsText.setFont(Font.font("Verdana", 18));

        javafx.scene.layout.HBox bottomBox = new javafx.scene.layout.HBox(30);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER);
        bottomBox.setPrefWidth(1100);
        bottomBox.setTranslateY(560);
        bottomBox.getChildren().addAll(vectorText, forceText, physicsText);

        addUINode(scoreboardBox);
        addUINode(turnText);
        addUINode(bottomBox);
        addUINode(goalText);
    }

    private void createPitchWalls() {
        Walls.createWall(60, 60, 980, 10); // Top
        Walls.createWall(60, 530, 980, 10); // Bottom
        Walls.createWall(67, 50, 10, 180); // Left top
        Walls.createWall(67, 370, 10, 180); // Left bottom
        Walls.createWall(1023, 50, 10, 180); // Right top
        Walls.createWall(1023, 370, 10, 180);// Right bottom

        Walls.createWall(10, 0, 10, 1000); // Left Goal
        Walls.createWall(1080, 0, 10, 1000); // Right Goal

        // --- NEW GOAL NET WALLS ---

        // Left Goal Posts (Top and Bottom)
        Walls.createWall(10, 220, 57, 10); // Left Goal Top net
        Walls.createWall(10, 370, 57, 10); // Left Goal Bottom net

        // Right Goal Posts (Top and Bottom)
        Walls.createWall(1023, 220, 57, 10); // Right Goal Top net
        Walls.createWall(1023, 370, 57, 10); // Right Goal Bottom net
    }

    @Override
    protected void onUpdate(double tpf) {
        if (gameState != GameState.MATCH)
            return;

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
            } else if (activeToken != null) {
                CustomPhysicsComponent phys = activeToken.getComponent(CustomPhysicsComponent.class);
                Point2D vel = phys.getVelocity();
                double vMag = vel.magnitude();

                Point2D accelVec = vel.subtract(lastVelocity).multiply(1.0 / tpf);
                double aMag = accelVec.magnitude();
                double mass = phys.getMass();
                double fMag = mass * aMag;

                lastVelocity = vel;

                double displayX = vel.getX();
                double displayY = -vel.getY();
                double angle = Math.toDegrees(Math.atan2(displayY, displayX));
                if (angle < 0)
                    angle += 360;

                if (vMag < 1) {
                    resetPhysicsText();
                } else {
                    forceText.setText(String.format("Force: %.1f N", fMag));
                    vectorText.setText(String.format("Vector: (%.1f, %.1f)   Angle: %.0f°", displayX, displayY, angle));
                    physicsText.setText(String.format("m: %.1f kg   a: %.1f   v: %.1f", mass, aMag, vMag));
                }
            }
        }
        checkGoals();
        updateUI();
    }

    private void checkGoals() {
        if (isGoalCelebration)
            return;

        getGameWorld().getEntitiesByType(TokenDragTest.EntityType.BALL).stream().findFirst().ifPresent(ball -> {
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

        boolean isGameOver = (scoreBlue >= 3 || scoreRed >= 3);

        goalText.setText("GOAL!!!");
        goalText.setFont(Font.font("Verdana", javafx.scene.text.FontWeight.BOLD, 100));
        goalText.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
        goalText.setTranslateX(getAppWidth() / 2.0 - 230);
        goalText.setTranslateY(700);
        goalText.setVisible(true);

        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                javafx.util.Duration.seconds(1.2), goalText);
        tt.setFromY(700);
        tt.setToY(320);
        tt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        tt.play();

        if (!isGameOver) {
            getGameTimer().runOnceAfter(() -> {
                goalText.setVisible(false);
                resetPitch(nextTurnBlue);
                isGoalCelebration = false;
            }, javafx.util.Duration.seconds(4));
        } else {
            getGameTimer().runOnceAfter(() -> {
                isGoalCelebration = false;
                canMove = true;
                gameState = GameState.WINNER;
                buildWinnerUI();
            }, javafx.util.Duration.seconds(4));
        }
    }

    private void resetPitch(boolean nextTurnBlue) {
        getGameWorld().getEntitiesByType(TokenDragTest.EntityType.BALL).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(TokenDragTest.EntityType.TEAM_BLUE).forEach(Entity::removeFromWorld);
        getGameWorld().getEntitiesByType(TokenDragTest.EntityType.TEAM_RED).forEach(Entity::removeFromWorld);

        for (int i = 0; i < 5; i++) {
            Token.createToken(250, 120 + i * 90, c1, TokenDragTest.EntityType.TEAM_BLUE);
            Token.createToken(810, 120 + i * 90, c2, TokenDragTest.EntityType.TEAM_RED);
        }

        Ball.createBall(getAppWidth() / 2.0 - 12, getAppHeight() / 2.0 - 12);

        canMove = true;
        isBlueTurn = nextTurnBlue;
    }

    private void updateUI() {
        if (turnText != null) {
            turnText.setText(canMove ? (isBlueTurn ? "P1's TURN" : "P2's TURN") : "MOVING...");
            turnText.setFill(Color.WHITE);
        }
        if (scoreBlueText != null) {
            scoreBlueText.setText(String.valueOf(scoreBlue));
            scoreRedText.setText(String.valueOf(scoreRed));
        }
    }

    private void resetPhysicsText() {
        if (forceText == null || vectorText == null || physicsText == null)
            return;
        forceText.setText("Force: 0.0 N");
        vectorText.setText("Vector: (0.0, 0.0)   Angle: 0°");
        physicsText.setText("m: 0.0 kg   a: 0.0   v: 0.0");
    }

    private void checkCustomCollisions() {
        var entities = getGameWorld().getEntitiesByComponent(CustomPhysicsComponent.class);

        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity e1 = entities.get(i);
                Entity e2 = entities.get(j);

                Point2D p1 = e1.getCenter();
                Point2D p2 = e2.getCenter();

                double r1 = e1.getType() == TokenDragTest.EntityType.BALL ? 16.0 : 24.0;
                double r2 = e2.getType() == TokenDragTest.EntityType.BALL ? 16.0 : 24.0;
                double minDistance = r1 + r2;

                double distance = p1.distance(p2);

                if (distance < minDistance && distance > 0.0001) {
                    CustomPhysicsComponent phys1 = e1.getComponent(CustomPhysicsComponent.class);
                    CustomPhysicsComponent phys2 = e2.getComponent(CustomPhysicsComponent.class);

                    double m1 = phys1.getMass();
                    double m2 = phys2.getMass();
                    double invM1 = 1.0 / m1, invM2 = 1.0 / m2;
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
                    if (speedOnNormal < 0)
                        continue;

                    double restitution = Math.min(phys1.getRestitution(), phys2.getRestitution());
                    double impulseScalar = -(1 + restitution) * speedOnNormal;
                    impulseScalar /= totalInvMass;

                    Point2D impulseVector = normal.multiply(impulseScalar);
                    phys1.setVelocity(v1.add(impulseVector.multiply(invM1)));
                    phys2.setVelocity(v2.subtract(impulseVector.multiply(invM2)));
                }
            }
        }

        var walls = getGameWorld().getEntitiesByType(TokenDragTest.EntityType.WALL);
        for (Entity e : entities) {
            CustomPhysicsComponent phys = e.getComponent(CustomPhysicsComponent.class);
            Point2D pos = e.getCenter();
            double radius = e.getType() == TokenDragTest.EntityType.BALL ? 16.0 : 24.0;

            for (Entity wall : walls) {
                double wx = wall.getX(), wy = wall.getY(), ww = wall.getWidth(), wh = wall.getHeight();

                double nearestX = Math.max(wx, Math.min(pos.getX(), wx + ww));
                double nearestY = Math.max(wy, Math.min(pos.getY(), wy + wh));

                double dx = pos.getX() - nearestX;
                double dy = pos.getY() - nearestY;
                double distanceSq = dx * dx + dy * dy;

                if (distanceSq < radius * radius && distanceSq > 0.0001) {
                    double distance = Math.sqrt(distanceSq);

                    double normalX = dx / distance;
                    double normalY = dy / distance;

                    double overlap = radius - distance;
                    e.translate(normalX * overlap, normalY * overlap);

                    Point2D vel = phys.getVelocity();
                    double dotProduct = vel.getX() * normalX + vel.getY() * normalY;

                    if (dotProduct < 0) {
                        double wallRestitution = 0.8;
                        double restitution = phys.getRestitution() * wallRestitution;
                        double scalar = -(1 + restitution) * dotProduct;
                        phys.setVelocity(vel.add(normalX * scalar, normalY * scalar));
                    }
                }
            }
        }
    }

    // --- PHASE 4: WINNER ---
    private void buildWinnerUI() {
        getGameScene().clearUINodes();
        getGameScene().clearGameViews();
        new java.util.ArrayList<>(getGameWorld().getEntities()).forEach(Entity::removeFromWorld);

        Image image = new Image(getClass().getResource("/assets/textures/winner.png").toExternalForm());
        ImageView bg = new ImageView(image);
        bg.setFitWidth(getAppWidth());
        bg.setFitHeight(getAppHeight());
        getGameScene().addUINode(bg);

        String winnerStr = scoreBlue >= 3 ? "Player 1 Wins!" : "Player 2 Wins!";
        Text winnerText = new Text(winnerStr);
        winnerText.setFill(Color.WHITE);
        winnerText.setStroke(Color.BLACK);
        winnerText.setStrokeWidth(2);
        winnerText.setTranslateX(getAppWidth() / 2.0 - 250);
        winnerText.setTranslateY(150);

        Button menuBtn = new Button("Main Menu");
        menuBtn.setPrefSize(200, 60);
        menuBtn.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue; -fx-text-fill: white;");
        menuBtn.setTranslateX(getAppWidth() / 2.0 - 100);
        menuBtn.setTranslateY(500);

        menuBtn.setOnAction(e -> {
            scoreBlue = 0;
            scoreRed = 0;
            gameState = GameState.MAIN_MENU;
            buildMainMenu();
        });

        getGameScene().addUINodes(winnerText, menuBtn);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
