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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminRHController {

    // Nombre maximum de RH autorises dans l'entreprise
    private static final int MAX_RH = 2;

    // Postes autorises a devenir RH
    private static final List<String> POSTES_AUTORISES_RH = Arrays.asList(
    	    "RH Manager"
    	);
    // Table
    @FXML private TableView<Utilisateur>            tableRH;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String>  colLogin;
    @FXML private TableColumn<Utilisateur, String>  colRole;
    @FXML private TableColumn<Utilisateur, Integer> colEmployeId;

    // Formulaire creation
    @FXML private ComboBox<Employe> comboEmploye;
    @FXML private TextField         loginField;
    @FXML private PasswordField     passwordField;
    @FXML private PasswordField     confirmPasswordField;

    // Formulaire modification
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
        rafraichirCombo(); // charge le combo en excluant les employes deja RH

        // Quand on selectionne un RH dans la table, remplir les champs de modification
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

    // *** RAFRAICHIR LE COMBO : exclure les employes qui ont deja un compte RH ***

    private void rafraichirCombo() {
        // Recuperer tous les employes
        List<Employe> tous = employeDAO.getTous();

        // Recuperer les IDs des employes qui ont deja un compte RH
        List<Integer> idsDejaRH = utilisateurDAO.getTousRH()
            .stream()
            .map(Utilisateur::getEmployeId)
            .collect(Collectors.toList());

        // Filtrer : exclure ceux qui sont deja RH
        List<Employe> disponibles = tous.stream()
            .filter(e -> !idsDejaRH.contains(e.getId()))
            .collect(Collectors.toList());

        // Filtrer aussi par poste autorise
        List<Employe> eligibles = disponibles.stream()
            .filter(e -> POSTES_AUTORISES_RH.stream()
                .anyMatch(p -> e.getPoste() != null &&
                               e.getPoste().toLowerCase().contains(p.toLowerCase())))
            .collect(Collectors.toList());

        // Si aucun eligible par poste, afficher tous les disponibles (pour eviter combo vide)
        List<Employe> afficher = eligibles;

        if (afficher.isEmpty()) {
            comboEmploye.setDisable(true);
            comboEmploye.setPromptText("Aucun employe eligible disponible");
        } else {
            comboEmploye.setItems(FXCollections.observableArrayList(afficher));
        }
        comboEmploye.setValue(null); // reinitialiser la selection

        // Style d'affichage dans le combo
        comboEmploye.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employe e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null
                    : e.getPrenom() + " " + e.getNom() + " — " + e.getPoste());
            }
        });
        comboEmploye.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employe e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null
                    : e.getPrenom() + " " + e.getNom() + " — " + e.getPoste());
            }
        });

        // Desactiver le combo si la limite est atteinte
        int nb = utilisateurDAO.compterRH();
        if (nb >= MAX_RH) {
            comboEmploye.setDisable(true);
            comboEmploye.setPromptText("Limite de " + MAX_RH + " RH atteinte");
        } else {
            comboEmploye.setDisable(false);
            comboEmploye.setPromptText("Selectionner un employe");
        }
    }

    // *** CREER UN RH ***

    @FXML
    public void creerRH() {
        // Verifier la limite
        if (utilisateurDAO.compterRH() >= MAX_RH) {
            afficherMessage("Limite atteinte : maximum " + MAX_RH
                + " comptes RH autorises dans l'entreprise.", false);
            return;
        }

        String login    = loginField.getText().trim();
        String mdp      = passwordField.getText().trim();
        String mdpConf  = confirmPasswordField.getText().trim();
        Employe employe = comboEmploye.getValue();

        if (login.isEmpty() || mdp.isEmpty() || mdpConf.isEmpty()) {
            afficherMessage("Veuillez remplir tous les champs.", false); return;
        }
        if (!mdp.equals(mdpConf)) {
            afficherMessage("Les mots de passe ne correspondent pas.", false); return;
        }
        if (mdp.length() < 6) {
            afficherMessage("Le mot de passe doit contenir au moins 6 caracteres.", false); return;
        }
        if (employe == null) {
            afficherMessage("Veuillez selectionner un employe.", false); return;
        }
        if (utilisateurDAO.loginExiste(login)) {
            afficherMessage("Ce login existe deja. Choisissez-en un autre.", false); return;
        }

        Utilisateur nvRH = new Utilisateur(0, login, mdp, Utilisateur.Role.RH, employe.getId());
        boolean ok = utilisateurDAO.ajouter(nvRH);

        if (ok) {
            afficherMessage("Compte RH \"" + login + "\" cree avec succes.", true);
            viderFormulaire();
            chargerTableRH();
            rafraichirCombo(); // mettre a jour le combo : retirer l'employe qui vient d'etre assigne
        } else {
            afficherMessage("Erreur lors de la creation du compte RH.", false);
        }
    }

    // *** MODIFIER UN RH SELECTIONNE ***

    @FXML
    public void modifierRH() {
        Utilisateur rh = tableRH.getSelectionModel().getSelectedItem();
        if (rh == null) {
            afficherMessage("Selectionnez un RH dans la table pour le modifier.", false);
            return;
        }

        String nouveauLogin = modifLoginField.getText().trim();
        String nouveauMdp   = modifPasswordField.getText().trim();

        if (nouveauLogin.isEmpty()) {
            afficherMessage("Le login ne peut pas etre vide.", false); return;
        }
        if (!nouveauLogin.equals(rh.getLogin()) && utilisateurDAO.loginExiste(nouveauLogin)) {
            afficherMessage("Ce login est deja utilise par un autre compte.", false); return;
        }

        boolean ok = utilisateurDAO.modifierRH(rh.getId(), nouveauLogin,
                nouveauMdp.isEmpty() ? null : nouveauMdp);

        if (ok) {
            afficherMessage("Compte RH modifie avec succes.", true);
            chargerTableRH();
        } else {
            afficherMessage("Erreur lors de la modification.", false);
        }
    }

    // *** SUPPRIMER UN RH SELECTIONNE ***

    @FXML
    public void supprimerRH() {
        Utilisateur rh = tableRH.getSelectionModel().getSelectedItem();
        if (rh == null) {
            afficherMessage("Selectionnez un RH a supprimer dans la table.", false); return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le compte RH");
        confirm.setContentText("Supprimer le compte \"" + rh.getLogin() + "\" ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        boolean ok = utilisateurDAO.supprimer(rh.getId());
        if (ok) {
            afficherMessage("Compte \"" + rh.getLogin() + "\" supprime.", true);
            modifLoginField.clear();
            modifPasswordField.clear();
            chargerTableRH();
            rafraichirCombo(); // l'employe supprime redevient disponible dans le combo
        } else {
            afficherMessage("Impossible de supprimer ce compte.", false);
        }
    }

    // *** RAFRAICHIR TOUT ***

    @FXML
    public void rafraichir() {
        chargerTableRH();
        rafraichirCombo();
        afficherMessage("Table actualisee.", true);
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