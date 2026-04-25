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
            // *** REDIRECTION SELON LE RÔLE ***
            if (utilisateur.isAdmin()) {
                // L'admin va vers son interface dédiée
                ouvrirInterface("/view/admin_main.fxml", utilisateur, "Administration — Gestion des Employés", 1100, 700);
            } else {
                // Le RH va vers l'interface RH existante
                ouvrirInterface("/view/main.fxml", utilisateur, "Gestion des Employés", 1000, 650);
            }

        } catch (Exception e) {
            e.printStackTrace();
            erreurLabel.setText("Erreur lors du chargement de l'interface.");
        }
    }

    private void ouvrirInterface(String fxmlPath, Utilisateur utilisateur, String titre, int largeur, int hauteur) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Passer l'utilisateur connecté au controller de la vue cible
        Object controller = loader.getController();
        if (controller instanceof AdminMainController) {
            ((AdminMainController) controller).setAdmin(utilisateur);
        } else if (controller instanceof MainController) {
            ((MainController) controller).setUtilisateur(utilisateur);
        }

        Stage stage = (Stage) loginField.getScene().getWindow();
        stage.setScene(new Scene(root, largeur, hauteur));
        stage.setTitle(titre);
        stage.show();
    }
}