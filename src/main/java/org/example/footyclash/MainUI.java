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
        gameSettings.setTitle("Footy Clash");
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
        
        Button startBtn = new Button("Start");
        startBtn.setPrefSize(250, 80);
        startBtn.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: blue; -fx-text-fill: white;");

        startBtn.setOnAction(e -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    new org.example.footyclash.TestingClasses.PlayerSelectTest().start(new javafx.stage.Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            javafx.stage.Stage stage = (javafx.stage.Stage) startBtn.getScene().getWindow();
            stage.close();
        });

        mmenu.getChildren().add(startBtn);

        mmenu.setTranslateX(650);
        mmenu.setTranslateY(getAppHeight() / 2 - mmenu.getHeight() / 2);

        getGameScene().addUINode(mmenu);

    }
}
