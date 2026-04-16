module org.example.footyclash {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires javafx.graphics;
    requires org.example.footyclash;
    requires com.almasb.fxgl.entity;

    opens org.example.footyclash to javafx.fxml;

    exports org.example.footyclash;
    exports GameClasses;
    exports org.example.footyclash.TestingClasses;
}