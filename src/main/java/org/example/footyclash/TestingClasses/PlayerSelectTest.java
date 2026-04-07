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
        Image image = new Image(
                getClass().getResource("/assets/textures/tokenselect.png").toExternalForm());

        ImageView bg = new ImageView(image);
        bg.setFitWidth(getAppWidth());
        bg.setFitHeight(getAppHeight());

        getGameScene().addUINode(bg);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
