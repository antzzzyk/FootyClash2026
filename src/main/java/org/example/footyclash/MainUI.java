package org.example.footyclash;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

//This Java Class will focus on the full game compilation
//This is where the full game will be located
//This is also where the UI is set up
public class MainUI extends GameApplication {

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1000);
        gameSettings.setHeight(600);
        gameSettings.setTitle("FootyClash");
    }

    @Override
    protected void initGame() {

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initUI() {
        Image image = new Image(
                getClass().getResource("/assets/textures/mainmenu.png").toExternalForm());

        ImageView bg = new ImageView(image);
        bg.setFitWidth(getAppWidth());
        bg.setFitHeight(getAppHeight());

        getGameScene().addUINode(bg);

        // Buttons
        VBox mmenu = new VBox(10);
        Button solo = new Button("Solo Mode");
        solo.setOnAction(e -> {
        });
        Button twop = new Button("2 Players");
        twop.setOnAction(e -> {
        });
        Button sett = new Button("Settings");

        // Apply sizing to both
        solo.setPrefSize(250, 80);
        twop.setPrefSize(250, 80);
        sett.setPrefSize(250, 80);
        solo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue; -fx-text-fill: white;");
        twop.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue; -fx-text-fill: white;");
        sett.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;-fx-background-color: blue; -fx-text-fill: white;");

        mmenu.getChildren().addAll(solo, twop, sett);

        mmenu.setTranslateX(650);
        mmenu.setTranslateY(getAppHeight() / 2 - mmenu.getHeight() / 2);

        getGameScene().addUINode(mmenu);

    }
}
