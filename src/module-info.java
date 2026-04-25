module gestion_employes {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens controller to javafx.fxml;
    opens view to javafx.fxml;
    opens main to javafx.graphics;

    exports main;
}