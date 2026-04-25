package controller;

import model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminMainController {

    @FXML private StackPane adminContentArea;
    @FXML private Label adminNomLabel;

    private Utilisateur adminConnecte;

    // Appelée par LoginController après chargement de la vue
    public void setAdmin(Utilisateur admin) {
        this.adminConnecte = admin;
        adminNomLabel.setText("👤 " + admin.getLogin() + " — Administrateur");
        showAdminDashboard(); // Afficher le dashboard par défaut
    }

    @FXML
    public void initialize() {
        // La vue est chargée mais setAdmin() n'a pas encore été appelée.
        // Ne rien faire ici car adminConnecte est null à ce stade.
    }

    // *** NAVIGATION ***

    @FXML
    public void showAdminDashboard() {
        chargerVue("/view/admin_dashboard.fxml");
    }

    @FXML
    public void showGestionRH() {
        chargerVue("/view/gestion_rh.fxml");
    }

    @FXML
    public void showParametres() {
        // Passer l'admin au controller des paramètres
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin_parametres.fxml"));
            Parent vue = loader.load();
            AdminParametresController ctrl = loader.getController();
            ctrl.setAdmin(adminConnecte);
            adminContentArea.getChildren().clear();
            adminContentArea.getChildren().add(vue);
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Impossible de charger les paramètres.");
        }
    }

    @FXML
    public void seDeconnecter() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) adminContentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400));
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // *** MÉTHODE PRIVÉE : chargement de vue simple ***

    private void chargerVue(String fxmlPath) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            adminContentArea.getChildren().clear();
            adminContentArea.getChildren().add(vue);
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Impossible de charger : " + fxmlPath);
        }
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}