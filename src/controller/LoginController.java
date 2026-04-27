package controller;

import dao.UtilisateurDAO;
import model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label erreurLabel;

    @FXML
    private void seConnecter() {
        String login = loginField.getText().trim();
        String motDePasse = passwordField.getText().trim();

        if (login.isEmpty() || motDePasse.isEmpty()) {
            erreurLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        UtilisateurDAO dao = new UtilisateurDAO();
        Utilisateur utilisateur = dao.authentifier(login, motDePasse);

        if (utilisateur == null) {
            erreurLabel.setText("Login ou mot de passe incorrect.");
            return;
        }

        try {
            Stage stage = (Stage) loginField.getScene().getWindow();

            if (utilisateur.isResponsable()) {
                // ── Interface Administrateur ──
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin_main.fxml"));
                Parent root = loader.load();

                AdminMainController adminController = loader.getController();
                adminController.setAdmin(utilisateur);

                stage.setScene(new Scene(root, 1100, 700));
                stage.setTitle("Administration – Gestion des Employés");

            } else {
                // ── Interface RH / Responsable ──
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
                Parent root = loader.load();

                MainController mainController = loader.getController();
                mainController.setUtilisateur(utilisateur);

                stage.setScene(new Scene(root, 1000, 650));
                stage.setTitle("Gestion des Employés");
            }

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}