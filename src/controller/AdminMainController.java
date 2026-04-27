package controller;

import model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminMainController {

    @FXML private StackPane adminContentArea;
    @FXML private Label adminNomLabel;

    private Utilisateur adminConnecte;

    public void setAdmin(Utilisateur admin) {
        this.adminConnecte = admin;
        adminNomLabel.setText("👤 " + admin.getLogin() + " — Administrateur");
        showAdminDashboard();
    }

    @FXML
    public void initialize() {
        // adminConnecte est null ici, ne rien faire
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
    public void showEmployes() {
        chargerVue("/view/admin_employes.fxml");
    }

    @FXML
    public void showParametres() {
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

    // *** DÉCONNEXION AVEC CONFIRMATION ***

    @FXML
    public void seDeconnecter() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Confirmer la déconnexion");
        confirm.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return; // L'admin a annulé
        }

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

    // *** UTILITAIRES ***

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