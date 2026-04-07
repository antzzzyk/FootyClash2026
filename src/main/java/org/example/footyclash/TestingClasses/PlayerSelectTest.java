package org.example.footyclash.TestingClasses;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppWidth;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

        // Dummy list of skin locations (you can update these paths when you have the
        // images)
        String[] skins = { "/assets/textures/plain.png", "/assets/textures/goal.png" };
        int[] p1SkinIndex = { 0 };
        int[] p2SkinIndex = { 0 };

        // --- PLAYER 1 SETUP ---
        javafx.scene.image.ImageView p1Token = new javafx.scene.image.ImageView();
        p1Token.setFitWidth(150);
        p1Token.setFitHeight(150);
        javafx.scene.shape.Circle clip1 = new javafx.scene.shape.Circle(75, 75, 75);
        p1Token.setClip(clip1);
        p1Token.setTranslateX(200); // Adjust this to center in the left grey rectangle
        p1Token.setTranslateY(225);

        javafx.scene.control.Button p1Prev = new javafx.scene.control.Button("<");
        p1Prev.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        p1Prev.setTranslateX(65);
        p1Prev.setTranslateY(260);

        javafx.scene.control.Button p1Next = new javafx.scene.control.Button(">");
        p1Next.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        p1Next.setTranslateX(290);
        p1Next.setTranslateY(260);

        p1Prev.setOnAction(e -> {
            p1SkinIndex[0] = (p1SkinIndex[0] - 1 + skins.length) % skins.length;
            // p1Token.setImage(new
            // Image(getClass().getResource(skins[p1SkinIndex[0]]).toExternalForm()));
        });

        p1Next.setOnAction(e -> {
            p1SkinIndex[0] = (p1SkinIndex[0] + 1) % skins.length;
            // p1Token.setImage(new
            // Image(getClass().getResource(skins[p1SkinIndex[0]]).toExternalForm()));
        });

        // --- PLAYER 2 SETUP ---
        javafx.scene.image.ImageView p2Token = new javafx.scene.image.ImageView();
        p2Token.setFitWidth(150);
        p2Token.setFitHeight(150);
        javafx.scene.shape.Circle clip2 = new javafx.scene.shape.Circle(75, 75, 75);
        p2Token.setClip(clip2);
        p2Token.setTranslateX(750); // Adjust this to center in the right grey rectangle
        p2Token.setTranslateY(225);

        javafx.scene.control.Button p2Prev = new javafx.scene.control.Button("<");
        p2Prev.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        p2Prev.setTranslateX(740);
        p2Prev.setTranslateY(260);

        javafx.scene.control.Button p2Next = new javafx.scene.control.Button(">");
        p2Next.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        p2Next.setTranslateX(965);
        p2Next.setTranslateY(260);

        p2Prev.setOnAction(e -> {
            p2SkinIndex[0] = (p2SkinIndex[0] - 1 + skins.length) % skins.length;
            // p2Token.setImage(new
            // Image(getClass().getResource(skins[p2SkinIndex[0]]).toExternalForm()));
        });

        p2Next.setOnAction(e -> {
            p2SkinIndex[0] = (p2SkinIndex[0] + 1) % skins.length;
            // p2Token.setImage(new
            // Image(getClass().getResource(skins[p2SkinIndex[0]]).toExternalForm()));
        });

        // Add nodes to the scene
        getGameScene().addUINodes(p1Token, p1Prev, p1Next, p2Token, p2Prev, p2Next);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
