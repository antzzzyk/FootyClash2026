package org.example.footyclash.TestingClasses;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

//This Class will focus
public class Testing extends GameApplication {
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Match UI");
        gameSettings.setWidth(1100);
        gameSettings.setHeight(600);

    }


    @Override
    protected void initGame() {

    }

    public  static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initUI(){
        Image pitch = new Image(
                getClass().getResource("/assets/textures/match/pitch.png").toExternalForm()
        );

        ImageView pitchbg = new ImageView(pitch);
        pitchbg.setFitWidth(getAppWidth());
        pitchbg.setFitHeight(getAppHeight());

        getGameScene().addUINode(pitchbg);

    }
}
