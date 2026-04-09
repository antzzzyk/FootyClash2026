package GameClasses;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Pitch {
    private static final float FRICTIONCOEFFICIENT = .8f;
    private Image image;
    private ImageView bg;

    public Pitch() {
        image = new Image(
                getClass().getResource("/assets/textures/match.png").toExternalForm());
        bg = new ImageView(image);
        bg.setFitWidth(1100);
        bg.setFitHeight(600);
    }

    public static float getFrictioncoefficient(){
        return FRICTIONCOEFFICIENT;
    }

    public ImageView getBg(){
        return bg;
    }
}
