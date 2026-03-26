module org.example.footyclash {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires javafx.graphics;

    opens org.example.footyclash to javafx.fxml;
    exports org.example.footyclash;
    exports org.example.footyclash.TestingClasses;
}