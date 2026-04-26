module gestion_employes {

    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;

    opens controller to javafx.fxml;
    opens model to javafx.base;
    opens main to javafx.fxml;

    exports main;
}