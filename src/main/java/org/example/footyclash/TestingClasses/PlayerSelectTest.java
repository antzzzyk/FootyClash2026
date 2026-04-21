package org.example.footyclash.TestingClasses;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppWidth;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

//This class is a test class where we try to implement the player selection screen
//This is where the player will select their tokens
/*
 * Things to implement:
 * - Two player selection screen
 * - The buttons allowing the users to select their tokens
 * - Buttons that changes tokens (slidebar kinda idk whats the name)
 */
public class PlayerSelectTest extends GameApplication {

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1100);
        gameSettings.setHeight(600);
        gameSettings.setTitle("Footy Clash - Player Select Test");
    }

    @Override
    protected void initGame() {

    }

    @Override
    protected void initUI() {

        // Adding the background image
        Image image = new Image(
                getClass().getResource("/assets/textures/tokenselect.png").toExternalForm());

        ImageView bg = new ImageView(image);
        bg.setFitWidth(getAppWidth());
        bg.setFitHeight(getAppHeight());

        getGameScene().addUINode(bg);

        // Adding the buttons that switches the tokens' image
        // THE TOKENS WILL BE IMAGEVIEW IN THE SHAPE OF CIRCLE
        // The tokens would in the center of the grey rectangles (run the class to see
        // the rectangles)
        // The buttons to swtich will in the middle, one at the left extreme another in
        // the right extreme

        // List of colors for tokens
        javafx.scene.paint.Color[] colors = {
                javafx.scene.paint.Color.BLUE,
                javafx.scene.paint.Color.RED,
                javafx.scene.paint.Color.GREEN,
                javafx.scene.paint.Color.YELLOW,
                javafx.scene.paint.Color.ORANGE,
                javafx.scene.paint.Color.PURPLE,
                javafx.scene.paint.Color.CYAN,
                javafx.scene.paint.Color.MAGENTA,
                javafx.scene.paint.Color.PINK
        };

        String[] colorNames = {
                "BLUE", "RED", "GREEN", "YELLOW", "ORANGE", "PURPLE", "CYAN", "MAGENTA", "PINK"
        };

        int[] p1ColorIndex = { 0 }; // Default BLUE
        int[] p2ColorIndex = { 1 }; // Default RED

        // --- PLAYER 1 SETUP ---
        Circle p1Circle = new Circle(75, 75, 75);
        p1Circle.setFill(colors[p1ColorIndex[0]]);
        p1Circle.setStroke(javafx.scene.paint.Color.BLACK);
        p1Circle.setStrokeWidth(3);
        p1Circle.setTranslateX(140); // Adjust this to center in the left grey rectangle
        p1Circle.setTranslateY(225);

        Label p1ColorText = new Label(colorNames[p1ColorIndex[0]]);
        p1ColorText.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
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

        // --- PLAYER 2 SETUP ---
        Circle p2Circle = new Circle(75, 75, 75);
        p2Circle.setFill(colors[p2ColorIndex[0]]);
        p2Circle.setStroke(javafx.scene.paint.Color.BLACK);
        p2Circle.setStrokeWidth(3);
        p2Circle.setTranslateX(825); // Adjust this to center in the right grey rectangle
        p2Circle.setTranslateY(225);

        Label p2ColorText = new Label(colorNames[p2ColorIndex[0]]);
        p2ColorText.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
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
        startButton.setTranslateX((FXGL.getAppWidth() - 250) / 2.0);
        startButton.setTranslateY(450);

        startButton.setOnAction(e -> {

            Color c1 = colors[p1ColorIndex[0]];
            Color c2 = colors[p2ColorIndex[0]];

            javafx.application.Platform.runLater(() -> {
                try {
                    new TokenDragTest(c1, c2).start(new javafx.stage.Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            javafx.stage.Stage stage = (javafx.stage.Stage) startButton.getScene().getWindow();
            stage.close();
        });

        // Add nodes to the scene
        getGameScene().addUINodes(p1ColorText, p1Circle, p1Prev, p1Next,
                p2ColorText, p2Circle, p2Prev, p2Next, startButton);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
