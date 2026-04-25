package controller;

import dao.EmployeDAO;
import dao.UtilisateurDAO;
import model.Employe;
import model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AdminRHController {

    // Table principale
    @FXML private TableView<Utilisateur> tableRH;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String>  colLogin;
    @FXML private TableColumn<Utilisateur, String>  colRole;
    @FXML private TableColumn<Utilisateur, Integer> colEmployeId;

    // Formulaire de création
    @FXML private ComboBox<Employe>   comboEmploye;
    @FXML private TextField            loginField;
    @FXML private PasswordField        passwordField;
    @FXML private PasswordField        confirmPasswordField;

    // Labels de retour
    @FXML private Label messageLabel;
    @FXML private Label compteurLabel;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final EmployeDAO      employeDAO    = new EmployeDAO();

    @FXML
    public void initialize() {
        // Configurer les colonnes de la table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colEmployeId.setCellValueFactory(new PropertyValueFactory<>("employeId"));

        // Charger les données
        chargerTableRH();
        chargerEmployesDansCombo();
    }

    // *** CHARGER LA TABLE ***

    private void chargerTableRH() {
        List<Utilisateur> liste = utilisateurDAO.getTousRH();
        ObservableList<Utilisateur> data = FXCollections.observableArrayList(liste);
        tableRH.setItems(data);
        int nb = utilisateurDAO.compterRH();
        if (compteurLabel != null) {
            compteurLabel.setText("Total RH actifs : " + nb);
        }
    }

    // *** CHARGER LES EMPLOYES DANS LE COMBO ***

    private void chargerEmployesDansCombo() {
        List<Employe> employes = employeDAO.getTous();
        comboEmploye.setItems(FXCollections.observableArrayList(employes));
        // Afficher le prénom + nom dans le combobox
        comboEmploye.setCellFactory(lv -> new ListCell<Employe>() {
            @Override
            protected void updateItem(Employe e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getPrenom() + " " + e.getNom());
            }
        });
        comboEmploye.setButtonCell(new ListCell<Employe>() {
            @Override
            protected void updateItem(Employe e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getPrenom() + " " + e.getNom());
            }
        });
    }

    // *** CRÉER UN NOUVEAU RH ***

    @FXML
    public void creerRH() {
        // Récupérer les valeurs du formulaire
        String login    = loginField.getText().trim();
        String mdp      = passwordField.getText().trim();
        String mdpConf  = confirmPasswordField.getText().trim();
        Employe employe = comboEmploye.getValue();

        // Validations
        if (login.isEmpty() || mdp.isEmpty() || mdpConf.isEmpty()) {
            afficherMessage("Veuillez remplir tous les champs.", "rouge");
            return;
        }
        if (!mdp.equals(mdpConf)) {
            afficherMessage("Les mots de passe ne correspondent pas.", "rouge");
            return;
        }
        if (mdp.length() < 6) {
            afficherMessage("Le mot de passe doit contenir au moins 6 caractères.", "rouge");
            return;
        }
        if (employe == null) {
            afficherMessage("Veuillez sélectionner un employé.", "rouge");
            return;
        }
        if (utilisateurDAO.loginExiste(login)) {
            afficherMessage("Ce login existe déjà. Choisissez-en un autre.", "rouge");
            return;
        }

        // Créer l'utilisateur RH
        Utilisateur nvRH = new Utilisateur(0, login, mdp, Utilisateur.Role.RH, employe.getId());
        boolean ok = utilisateurDAO.ajouter(nvRH);

        if (ok) {
            afficherMessage("✅ Compte RH \"" + login + "\" créé avec succès.", "vert");
            viderFormulaire();
            chargerTableRH();
        } else {
            afficherMessage("❌ Erreur lors de la création du compte RH.", "rouge");
        }
    }

    // *** SUPPRIMER UN RH SÉLECTIONNÉ ***

    @FXML
    public void supprimerRH() {
        Utilisateur rh = tableRH.getSelectionModel().getSelectedItem();
        if (rh == null) {
            afficherMessage("Sélectionnez un RH à supprimer dans la table.", "rouge");
            return;
        }

        // Demander confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le compte RH");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer le compte \"" + rh.getLogin() + "\" ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        boolean ok = utilisateurDAO.supprimer(rh.getId());
        if (ok) {
            afficherMessage("✅ Compte RH \"" + rh.getLogin() + "\" supprimé.", "vert");
            chargerTableRH();
        } else {
            afficherMessage("❌ Impossible de supprimer ce compte.", "rouge");
        }
    }

    // *** RAFRAÎCHIR LA TABLE ***

    @FXML
    public void rafraichir() {
        chargerTableRH();
        afficherMessage("Table actualisée.", "neutre");
    }

    // *** UTILITAIRES ***

    private void viderFormulaire() {
        loginField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        comboEmploye.setValue(null);
    }

    private void afficherMessage(String texte, String couleur) {
        if (messageLabel == null) return;
        messageLabel.setText(texte);
        switch (couleur) {
            case "rouge"  -> messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
            case "vert"   -> messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px;");
            default       -> messageLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        }
    }
}