package controller;

import dao.EmployeDAO;
import dao.UtilisateurDAO;
import model.Employe;
import model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Arrays;
import java.util.List;

public class AdminRHController {

    // Nombre maximum de RH autorisés dans l'entreprise
    private static final int MAX_RH = 2;

    // Postes autorisés à devenir RH
    private static final List<String> POSTES_AUTORISES_RH = Arrays.asList(
        "Responsable RH", "Chargé RH", "Assistant RH",
        "Directeur RH", "Gestionnaire RH", "RH"
    );

    // Table
    @FXML private TableView<Utilisateur>          tableRH;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String>  colLogin;
    @FXML private TableColumn<Utilisateur, String>  colRole;
    @FXML private TableColumn<Utilisateur, Integer> colEmployeId;

    // Formulaire
    @FXML private ComboBox<Employe> comboEmploye;
    @FXML private TextField          loginField;
    @FXML private PasswordField      passwordField;
    @FXML private PasswordField      confirmPasswordField;

    // Modification
    @FXML private TextField     modifLoginField;
    @FXML private PasswordField modifPasswordField;

    // Labels
    @FXML private Label messageLabel;
    @FXML private Label compteurLabel;
    @FXML private Label limiteLabel;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final EmployeDAO     employeDAO     = new EmployeDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colEmployeId.setCellValueFactory(new PropertyValueFactory<>("employeId"));

        chargerTableRH();
        chargerEmployesDansCombo();

        // Quand on sélectionne un RH dans la table, remplir les champs de modification
        tableRH.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) {
                modifLoginField.setText(nouveau.getLogin());
                modifPasswordField.clear();
            }
        });
    }

    // *** CHARGER TABLE ***

    private void chargerTableRH() {
        List<Utilisateur> liste = utilisateurDAO.getTousRH();
        tableRH.setItems(FXCollections.observableArrayList(liste));

        int nb = utilisateurDAO.compterRH();
        if (compteurLabel != null)
            compteurLabel.setText("RH actifs : " + nb + " / " + MAX_RH);
        if (limiteLabel != null)
            limiteLabel.setText("Limite maximale : " + MAX_RH + " comptes RH");
    }

    // *** CHARGER COMBO — seulement les employés avec poste autorisé ***

    private void chargerEmployesDansCombo() {
        List<Employe> tous = employeDAO.getTous();

        // Filtrer : seulement les postes autorisés à devenir RH
        List<Employe> eligibles = tous.stream()
            .filter(e -> POSTES_AUTORISES_RH.stream()
                .anyMatch(p -> e.getPoste() != null &&
                               e.getPoste().toLowerCase().contains(p.toLowerCase())))
            .toList();

        // Si aucun employé éligible, prendre tous (pour éviter combo vide en dev)
        List<Employe> afficher = eligibles.isEmpty() ? tous : eligibles;

        comboEmploye.setItems(FXCollections.observableArrayList(afficher));

        comboEmploye.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Employe e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null
                    : e.getPrenom() + " " + e.getNom() + " — " + e.getPoste());
            }
        });
        comboEmploye.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Employe e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null
                    : e.getPrenom() + " " + e.getNom() + " — " + e.getPoste());
            }
        });
    }

    // *** CRÉER UN RH ***

    @FXML
    public void creerRH() {
        // Vérifier la limite
        if (utilisateurDAO.compterRH() >= MAX_RH) {
            afficherMessage("❌ Limite atteinte : maximum " + MAX_RH
                + " comptes RH autorisés dans l'entreprise.", false);
            return;
        }

        String login   = loginField.getText().trim();
        String mdp     = passwordField.getText().trim();
        String mdpConf = confirmPasswordField.getText().trim();
        Employe employe = comboEmploye.getValue();

        if (login.isEmpty() || mdp.isEmpty() || mdpConf.isEmpty()) {
            afficherMessage("Veuillez remplir tous les champs.", false); return;
        }
        if (!mdp.equals(mdpConf)) {
            afficherMessage("Les mots de passe ne correspondent pas.", false); return;
        }
        if (mdp.length() < 6) {
            afficherMessage("Le mot de passe doit contenir au moins 6 caractères.", false); return;
        }
        if (employe == null) {
            afficherMessage("Veuillez sélectionner un employé.", false); return;
        }
        if (utilisateurDAO.loginExiste(login)) {
            afficherMessage("Ce login existe déjà. Choisissez-en un autre.", false); return;
        }

        Utilisateur nvRH = new Utilisateur(0, login, mdp, Utilisateur.Role.RH, employe.getId());
        boolean ok = utilisateurDAO.ajouter(nvRH);

        if (ok) {
            afficherMessage("✅ Compte RH \"" + login + "\" créé avec succès.", true);
            viderFormulaire();
            chargerTableRH();
        } else {
            afficherMessage("❌ Erreur lors de la création du compte RH.", false);
        }
    }

    // *** MODIFIER UN RH SÉLECTIONNÉ ***

    @FXML
    public void modifierRH() {
        Utilisateur rh = tableRH.getSelectionModel().getSelectedItem();
        if (rh == null) {
            afficherMessage("Sélectionnez un RH dans la table pour le modifier.", false);
            return;
        }

        String nouveauLogin = modifLoginField.getText().trim();
        String nouveauMdp   = modifPasswordField.getText().trim();

        if (nouveauLogin.isEmpty()) {
            afficherMessage("Le login ne peut pas être vide.", false); return;
        }

        // Vérifier login unique si changé
        if (!nouveauLogin.equals(rh.getLogin()) && utilisateurDAO.loginExiste(nouveauLogin)) {
            afficherMessage("Ce login est déjà utilisé par un autre compte.", false); return;
        }

        boolean ok = utilisateurDAO.modifierRH(rh.getId(), nouveauLogin,
                nouveauMdp.isEmpty() ? null : nouveauMdp);

        if (ok) {
            afficherMessage("✅ Compte RH modifié avec succès.", true);
            chargerTableRH();
        } else {
            afficherMessage("❌ Erreur lors de la modification.", false);
        }
    }

    // *** SUPPRIMER UN RH SÉLECTIONNÉ ***

    @FXML
    public void supprimerRH() {
        Utilisateur rh = tableRH.getSelectionModel().getSelectedItem();
        if (rh == null) {
            afficherMessage("Sélectionnez un RH à supprimer dans la table.", false); return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le compte RH");
        confirm.setContentText("Supprimer le compte \"" + rh.getLogin() + "\" ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        boolean ok = utilisateurDAO.supprimer(rh.getId());
        if (ok) {
            afficherMessage("✅ Compte \"" + rh.getLogin() + "\" supprimé.", true);
            modifLoginField.clear();
            modifPasswordField.clear();
            chargerTableRH();
        } else {
            afficherMessage("❌ Impossible de supprimer ce compte.", false);
        }
    }

    // *** RAFRAÎCHIR ***

    @FXML
    public void rafraichir() {
        chargerTableRH();
        chargerEmployesDansCombo();
        afficherMessage("Table actualisée.", true);
    }

    // *** UTILITAIRES ***

    private void viderFormulaire() {
        loginField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        comboEmploye.setValue(null);
    }

    private void afficherMessage(String texte, boolean succes) {
        if (messageLabel == null) return;
        messageLabel.setText(texte);
        messageLabel.setStyle(succes
            ? "-fx-text-fill: #27ae60; -fx-font-size: 13px;"
            : "-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
    }
}