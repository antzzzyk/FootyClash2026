package GameClasses;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Pitch {
    private static final float FRICTIONCOEFFICIENT = 3f;
    private static final float DAMPINGCOEFFICITENT = 1f;
    private static final float RESTIRUTION = 0.7f;
    private Image image;
    private ImageView bg;
    private Image goalImage;
    private ImageView goalImageView;

    public Pitch() {
        image = new Image(
                getClass().getResource("/assets/textures/match.png").toExternalForm());
        bg = new ImageView(image);
        bg.setFitWidth(1100);
        bg.setFitHeight(600);

        try {
            goalImage = new Image(getClass().getResource("/assets/textures/goal.png").toExternalForm());
            goalImageView = new ImageView(goalImage);
        } catch (Exception e) {
            goalImageView = new ImageView();
            System.err.println("Place your goal.png image in src/main/resources/assets/textures/goal.png");
        }
        goalImageView.setFitWidth(500);
        goalImageView.setFitHeight(250);
        goalImageView.setTranslateX((1100 - 500) / 2.0);
        goalImageView.setTranslateY((600 - 250) / 2.0);
        goalImageView.setVisible(false);
    }

    public static float getFrictioncoefficient(){
        return FRICTIONCOEFFICIENT;
    }

    public static float getDampingcoefficient(){
        return DAMPINGCOEFFICITENT;
    }

    public static float getRESTIRUTION(){
        return RESTIRUTION;
    }

    public ImageView getBg(){
        return bg;
    }

    public ImageView getGoalImageView(){
        return goalImageView;
    }
}
