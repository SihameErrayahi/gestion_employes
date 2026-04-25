package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label totalEmployes;

    @FXML
    public void initialize() {

        // simulation données RH
        totalEmployes.setText("24");
    }
}