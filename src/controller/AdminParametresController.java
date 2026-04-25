package controller;

import dao.UtilisateurDAO;
import model.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class AdminParametresController {

    @FXML private PasswordField ancienMdpField;
    @FXML private PasswordField nouveauMdpField;
    @FXML private PasswordField confirmNouveauMdpField;
    @FXML private Label         messageLabel;
    @FXML private Label         infoAdminLabel;

    private Utilisateur adminConnecte;
    private final UtilisateurDAO dao = new UtilisateurDAO();

    public void setAdmin(Utilisateur admin) {
        this.adminConnecte = admin;
        if (infoAdminLabel != null) {
            infoAdminLabel.setText("Compte : " + admin.getLogin() + "  |  Rôle : ADMINISTRATEUR");
        }
    }

    @FXML
    public void changerMotDePasse() {
        if (adminConnecte == null) {
            afficherMessage("Erreur : aucun administrateur connecté.", false);
            return;
        }

        String ancien  = ancienMdpField.getText().trim();
        String nouveau = nouveauMdpField.getText().trim();
        String confirm = confirmNouveauMdpField.getText().trim();

        if (ancien.isEmpty() || nouveau.isEmpty() || confirm.isEmpty()) {
            afficherMessage("Veuillez remplir tous les champs.", false);
            return;
        }
        if (!nouveau.equals(confirm)) {
            afficherMessage("Les nouveaux mots de passe ne correspondent pas.", false);
            return;
        }
        if (nouveau.length() < 6) {
            afficherMessage("Le nouveau mot de passe doit faire au moins 6 caractères.", false);
            return;
        }
        if (nouveau.equals(ancien)) {
            afficherMessage("Le nouveau mot de passe doit être différent de l'ancien.", false);
            return;
        }

        boolean ok = dao.modifierMotDePasse(adminConnecte.getId(), ancien, nouveau);

        if (ok) {
            afficherMessage("✅ Mot de passe modifié avec succès.", true);
            ancienMdpField.clear();
            nouveauMdpField.clear();
            confirmNouveauMdpField.clear();
        } else {
            afficherMessage("❌ Ancien mot de passe incorrect.", false);
        }
    }

    private void afficherMessage(String texte, boolean succes) {
        if (messageLabel == null) return;
        messageLabel.setText(texte);
        messageLabel.setStyle(succes
            ? "-fx-text-fill: #27ae60; -fx-font-size: 13px;"
            : "-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
    }
}