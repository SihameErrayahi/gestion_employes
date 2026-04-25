package controller;

import model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label utilisateurLabel;

    private Utilisateur utilisateurConnecte;

    public void setUtilisateur(Utilisateur u) {
        this.utilisateurConnecte = u;
        utilisateurLabel.setText(u.getLogin() + " (" + u.getRole() + ")");
        showDashboard();
    }

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        chargerVue("/view/dashboard.fxml");
    }

    @FXML
    public void showEmploye() {
        chargerVue("/view/employe.fxml");
    }

    @FXML
    public void showSalaire() {
        chargerVue("/view/salaire.fxml");
    }

    @FXML
    public void showConge() {
        chargerVue("/view/conge.fxml");
    }

    private void chargerVue(String fxmlPath) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vue);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger : " + fxmlPath);
            alert.showAndWait();
        }
    }
}