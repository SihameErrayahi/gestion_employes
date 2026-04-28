package controller;

import model.Employe;
import service.EmployeService;
import service.EmployeService.ResultatService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

public class EmployeController {

    // ─── TABLE ──────────────────────────────────────────────────────────────
    @FXML private TableView<Employe>               tableEmployes;
    @FXML private TableColumn<Employe, Integer>    colId;
    @FXML private TableColumn<Employe, String>     colNom;
    @FXML private TableColumn<Employe, String>     colPrenom;
    @FXML private TableColumn<Employe, String>     colPoste;
    @FXML private TableColumn<Employe, String>     colDepartement;
    @FXML private TableColumn<Employe, String>     colEmail;
    @FXML private TableColumn<Employe, String>     colTelephone;
    @FXML private TableColumn<Employe, String>     colDateEmbauche;
    @FXML private TableColumn<Employe, Double>     colSalaireBase;
    @FXML private TableColumn<Employe, String>     colStatut;
    @FXML private TableColumn<Employe, String>     colAnciennete;

    // ─── FORMULAIRE ─────────────────────────────────────────────────────────
    @FXML private TextField    fieldNom;
    @FXML private TextField    fieldPrenom;
    @FXML private TextField    fieldEmail;
    @FXML private TextField    fieldTelephone;
    @FXML private TextField    fieldPoste;
    @FXML private TextField    fieldDepartement;
    @FXML private DatePicker   fieldDateEmbauche;
    @FXML private TextField    fieldSalaireBase;
    @FXML private TextField    fieldCin;
    @FXML private TextField    fieldAdresse;
    @FXML private ComboBox<Employe.Statut> comboStatut;

    // ─── LABELS D'ERREUR PAR CHAMP ──────────────────────────────────────────
    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errEmail;
    @FXML private Label errTelephone;
    @FXML private Label errAdresse;

    // ─── RECHERCHE & MESSAGES ───────────────────────────────────────────────
    @FXML private TextField    fieldRecherche;
    @FXML private TextArea     labelMessage;
    @FXML private Label        labelNbEmployes;

    private final EmployeService employeService = new EmployeService();
    private ObservableList<Employe> listeEmployes;
    private FilteredList<Employe>   listeFiltree;

    // ════════════════════════ INIT ════════════════════════════════════════════
    @FXML
    public void initialize() {

        // ── Colonnes table ───────────────────────────────────────────────────
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDateEmbauche.setCellValueFactory(new PropertyValueFactory<>("dateEmbauche"));
        colSalaireBase.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));

        colStatut.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatut().name()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "ACTIF"    -> setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                    case "INACTIF"  -> setStyle("-fx-text-fill: #94A3B8;");
                    case "SUSPENDU" -> setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    default         -> setStyle("");
                }
            }
        });

        colAnciennete.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getAnciennete() + " ans"));

        // ── ComboBox statut ──────────────────────────────────────────────────
        comboStatut.setItems(FXCollections.observableArrayList(Employe.Statut.values()));
        comboStatut.setValue(Employe.Statut.ACTIF);

        // ── Data ─────────────────────────────────────────────────────────────
        listeEmployes = FXCollections.observableArrayList();
        listeFiltree  = new FilteredList<>(listeEmployes, p -> true);
        tableEmployes.setItems(listeFiltree);

        fieldRecherche.textProperty().addListener((obs, a, n) -> filtrer(n));
        tableEmployes.getSelectionModel().selectedItemProperty().addListener(
            (obs, ancien, nouveau) -> remplirFormulaire(nouveau));

        // ── Validation en temps réel ─────────────────────────────────────────
        configurerValidationChampsSequentiels();

        chargerEmployes();
    }

    // ════════════════════════ VALIDATION TEMPS RÉEL ═══════════════════════════

    /**
     * Configure la chaîne de validation : chaque champ n'est activé que si
     * le champ précédent est valide. Les erreurs s'affichent sous chaque champ.
     *
     * Ordre : Nom → Prénom → Email → Téléphone → Poste → Département
     *       → DateEmbauche → SalaireBase → CIN → Adresse → Statut
     */
    private void configurerValidationChampsSequentiels() {

        // ── NOM : lettres uniquement, min 2 caractères ────────────────────────
        fieldNom.textProperty().addListener((obs, ancien, nouveau) -> {
            String val = nouveau.trim();
            if (val.isEmpty()) {
                setErreur(errNom, "");
                desactiverDepuis(fieldPrenom);
            } else if (!val.matches("[\\p{L}\\s'\\-]+")) {
                setErreur(errNom, "⚠ Uniquement des lettres (pas de chiffres ni symboles).");
                desactiverDepuis(fieldPrenom);
            } else if (val.length() < 2) {
                setErreur(errNom, "⚠ Minimum 2 caractères.");
                desactiverDepuis(fieldPrenom);
            } else {
                setSucces(errNom);
                fieldPrenom.setDisable(false);
                // re-déclencher la validation du prénom pour propager la chaîne
                revaliderPrenom();
            }
        });

        // ── PRÉNOM : lettres uniquement, min 2 caractères ────────────────────
        fieldPrenom.textProperty().addListener((obs, ancien, nouveau) -> {
            revaliderPrenom();
        });

        // ── EMAIL : doit contenir @ + lettres/chiffres ────────────────────────
        fieldEmail.textProperty().addListener((obs, ancien, nouveau) -> {
            revaliderEmail();
        });

        // ── TÉLÉPHONE : commence par 06, exactement 10 chiffres ──────────────
        fieldTelephone.textProperty().addListener((obs, ancien, nouveau) -> {
            revaliderTelephone();
        });

        // ── ADRESSE : doit contenir lettres ET chiffres ───────────────────────
        fieldAdresse.textProperty().addListener((obs, ancien, nouveau) -> {
            revaliderAdresse();
        });
    }

    /** Valide le prénom et active/désactive la suite. */
    private void revaliderPrenom() {
        if (fieldPrenom.isDisable()) return;
        String val = fieldPrenom.getText().trim();
        if (val.isEmpty()) {
            setErreur(errPrenom, "");
            desactiverDepuis(fieldEmail);
        } else if (!val.matches("[\\p{L}\\s'\\-]+")) {
            setErreur(errPrenom, "⚠ Uniquement des lettres (pas de chiffres ni symboles).");
            desactiverDepuis(fieldEmail);
        } else if (val.length() < 2) {
            setErreur(errPrenom, "⚠ Minimum 2 caractères.");
            desactiverDepuis(fieldEmail);
        } else {
            setSucces(errPrenom);
            fieldEmail.setDisable(false);
            revaliderEmail();
        }
    }

    /** Valide l'email et active/désactive la suite. */
    private void revaliderEmail() {
        if (fieldEmail.isDisable()) return;
        String val = fieldEmail.getText().trim();
        if (val.isEmpty()) {
            setErreur(errEmail, "");
            desactiverDepuis(fieldTelephone);
        } else if (!val.contains("@")) {
            setErreur(errEmail, "⚠ L'email doit contenir un '@'.");
            desactiverDepuis(fieldTelephone);
        } else if (!val.matches("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")) {
            setErreur(errEmail, "⚠ Format invalide (ex: nom@domaine.com).");
            desactiverDepuis(fieldTelephone);
        } else {
            setSucces(errEmail);
            fieldTelephone.setDisable(false);
            revaliderTelephone();
        }
    }

    /** Valide le téléphone et active/désactive la suite. */
    private void revaliderTelephone() {
        if (fieldTelephone.isDisable()) return;
        String val = fieldTelephone.getText().trim();
        if (val.isEmpty()) {
            setErreur(errTelephone, "");
            desactiverDepuis(fieldPoste);
        } else if (!val.matches("\\d+")) {
            setErreur(errTelephone, "⚠ Uniquement des chiffres.");
            desactiverDepuis(fieldPoste);
        } else if (!val.startsWith("06")) {
            setErreur(errTelephone, "⚠ Doit commencer par 06.");
            desactiverDepuis(fieldPoste);
        } else if (val.length() != 10) {
            setErreur(errTelephone, "⚠ Doit contenir exactement 10 chiffres (actuellement : " + val.length() + ").");
            desactiverDepuis(fieldPoste);
        } else {
            setSucces(errTelephone);
            // Activer tous les champs restants
            fieldPoste.setDisable(false);
            fieldDepartement.setDisable(false);
            fieldDateEmbauche.setDisable(false);
            fieldSalaireBase.setDisable(false);
            fieldCin.setDisable(false);
            fieldAdresse.setDisable(false);
            comboStatut.setDisable(false);
        }
    }

    /** Valide l'adresse (doit contenir lettres ET chiffres). */
    private void revaliderAdresse() {
        if (fieldAdresse.isDisable()) return;
        String val = fieldAdresse.getText().trim();
        if (val.isEmpty()) {
            setErreur(errAdresse, "");
        } else if (!val.matches(".*[\\p{L}].*") || !val.matches(".*\\d.*")) {
            setErreur(errAdresse, "⚠ L'adresse doit contenir des lettres ET des chiffres (ex: 12 Rue Hassan).");
        } else {
            setSucces(errAdresse);
        }
    }

    /**
     * Désactive tous les champs à partir du champ donné dans la chaîne.
     * Chaîne : Prénom → Email → Téléphone → Poste → Département
     *        → DateEmbauche → SalaireBase → CIN → Adresse → Statut
     */
    private void desactiverDepuis(javafx.scene.control.Control depuis) {
        boolean desactiver = false;
        javafx.scene.control.Control[] chaine = {
            fieldPrenom, fieldEmail, fieldTelephone,
            fieldPoste, fieldDepartement, fieldDateEmbauche,
            fieldSalaireBase, fieldCin, fieldAdresse, comboStatut
        };
        for (javafx.scene.control.Control c : chaine) {
            if (c == depuis) desactiver = true;
            if (desactiver) {
                c.setDisable(true);
                // Vider le texte si c'est un TextField
                if (c instanceof TextField tf) tf.clear();
                if (c instanceof DatePicker dp) dp.setValue(null);
            }
        }
        // Effacer les messages d'erreur des champs désactivés
        if (depuis == fieldPrenom || depuis == fieldEmail ||
            depuis == fieldTelephone || depuis == fieldAdresse) {
            effacerErreursApres(depuis);
        }
    }

    private void effacerErreursApres(javafx.scene.control.Control depuis) {
        Label[] labels = { errPrenom, errEmail, errTelephone, errAdresse };
        javafx.scene.control.Control[] champs = { fieldPrenom, fieldEmail, fieldTelephone, fieldAdresse };
        boolean effacer = false;
        for (int i = 0; i < champs.length; i++) {
            if (champs[i] == depuis) effacer = true;
            if (effacer) labels[i].setText("");
        }
    }

    // ─── Helpers style erreur/succès ─────────────────────────────────────────
    private void setErreur(Label label, String msg) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px;");
    }

    private void setSucces(Label label) {
        label.setText("✔");
        label.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-font-weight: bold;");
    }

    // ════════════════════════ CHARGEMENT ══════════════════════════════════════
    private void chargerEmployes() {
        listeEmployes.setAll(employeService.getTousLesEmployes());
        majCompteur();
    }

    private void majCompteur() {
        int total = listeEmployes.size();
        long actifs = listeEmployes.stream()
            .filter(e -> e.getStatut() == Employe.Statut.ACTIF).count();
        labelNbEmployes.setText("Total : " + total + " employés  |  Actifs : " + actifs);
    }

    // ════════════════════════ FILTRAGE ════════════════════════════════════════
    private void filtrer(String motCle) {
        if (motCle == null || motCle.isBlank()) {
            listeFiltree.setPredicate(p -> true);
        } else {
            String mc = motCle.toLowerCase();
            listeFiltree.setPredicate(e ->
                e.getNom().toLowerCase().contains(mc) ||
                e.getPrenom().toLowerCase().contains(mc) ||
                e.getPoste().toLowerCase().contains(mc) ||
                e.getDepartement().toLowerCase().contains(mc) ||
                e.getEmail().toLowerCase().contains(mc)
            );
        }
    }

    // ════════════════════════ AJOUTER ═════════════════════════════════════════
    @FXML
    private void ajouterEmploye() {
        Employe e = construireDepuisForm(0);
        if (e == null) return;

        ResultatService resultat = employeService.ajouterEmploye(e);
        if (resultat.succes) {
            chargerEmployes();
            viderChamps();
            succes(resultat.message);
        } else {
            erreur(resultat.message);
        }
    }

    // ════════════════════════ MODIFIER ════════════════════════════════════════
    @FXML
    private void modifierEmploye() {
        Employe selectionne = tableEmployes.getSelectionModel().getSelectedItem();
        if (selectionne == null) {
            erreur("⚠ Veuillez sélectionner un employé à modifier.");
            return;
        }

        Employe e = construireDepuisForm(selectionne.getId());
        if (e == null) return;

        ResultatService resultat = employeService.modifierEmploye(e);
        if (resultat.succes) {
            chargerEmployes();
            succes(resultat.message);
        } else {
            erreur(resultat.message);
        }
    }

    // ════════════════════════ SUPPRIMER ═══════════════════════════════════════
    @FXML
    private void supprimerEmploye() {
        Employe selectionne = tableEmployes.getSelectionModel().getSelectedItem();
        if (selectionne == null) {
            erreur("⚠ Veuillez sélectionner un employé à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Confirmer la suppression de " + selectionne.getNomComplet() + " ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                ResultatService resultat = employeService.supprimerEmploye(selectionne.getId());
                if (resultat.succes) {
                    chargerEmployes();
                    viderChamps();
                    succes(resultat.message);
                } else {
                    erreur(resultat.message);
                }
            }
        });
    }

    // ════════════════════════ HELPERS FORMULAIRE ══════════════════════════════
    private Employe construireDepuisForm(int id) {
        double salaire = 0;
        String salaireText = fieldSalaireBase.getText().trim();
        if (!salaireText.isEmpty()) {
            try {
                salaire = Double.parseDouble(salaireText.replace(",", "."));
            } catch (NumberFormatException ex) {
                erreur("❌ Le salaire de base doit être un nombre (ex : 5000 ou 5000.50).");
                return null;
            }
        }

        return new Employe(
            id,
            fieldNom.getText().trim(),
            fieldPrenom.getText().trim(),
            fieldEmail.getText().trim(),
            fieldTelephone.getText().trim(),
            fieldPoste.getText().trim(),
            fieldDepartement.getText().trim(),
            fieldDateEmbauche.getValue() != null ? fieldDateEmbauche.getValue().toString() : "",
            salaire,
            fieldCin.getText().trim(),
            fieldAdresse.getText().trim(),
            comboStatut.getValue() != null ? comboStatut.getValue() : Employe.Statut.ACTIF
        );
    }

    /**
     * Remplit le formulaire depuis un employé sélectionné dans la table.
     * Active tous les champs pour permettre la modification.
     */
    private void remplirFormulaire(Employe e) {
        if (e == null) return;

        // Activer tous les champs avant de remplir
        fieldPrenom.setDisable(false);
        fieldEmail.setDisable(false);
        fieldTelephone.setDisable(false);
        fieldPoste.setDisable(false);
        fieldDepartement.setDisable(false);
        fieldDateEmbauche.setDisable(false);
        fieldSalaireBase.setDisable(false);
        fieldCin.setDisable(false);
        fieldAdresse.setDisable(false);
        comboStatut.setDisable(false);

        fieldNom.setText(e.getNom());
        fieldPrenom.setText(e.getPrenom());
        fieldEmail.setText(e.getEmail());
        fieldTelephone.setText(e.getTelephone());
        fieldPoste.setText(e.getPoste());
        fieldDepartement.setText(e.getDepartement());
        try { fieldDateEmbauche.setValue(LocalDate.parse(e.getDateEmbauche())); }
        catch (Exception ignored) { fieldDateEmbauche.setValue(null); }
        fieldSalaireBase.setText(String.valueOf(e.getSalaireBase()));
        fieldCin.setText(e.getCin());
        fieldAdresse.setText(e.getAdresse());
        comboStatut.setValue(e.getStatut());

        // Afficher ✔ sur tous les champs déjà remplis
        setSucces(errNom);
        setSucces(errPrenom);
        setSucces(errEmail);
        setSucces(errTelephone);
        setSucces(errAdresse);

        labelMessage.setText("");
        labelMessage.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }

    @FXML
    private void viderChamps() {
        // Vider tous les champs
        fieldNom.clear();
        fieldPrenom.clear();
        fieldEmail.clear();
        fieldTelephone.clear();
        fieldPoste.clear();
        fieldDepartement.clear();
        fieldDateEmbauche.setValue(null);
        fieldSalaireBase.clear();
        fieldCin.clear();
        fieldAdresse.clear();
        comboStatut.setValue(Employe.Statut.ACTIF);
        fieldRecherche.clear();
        tableEmployes.getSelectionModel().clearSelection();

        // Remettre la chaîne : tout désactiver sauf Nom
        fieldPrenom.setDisable(true);
        fieldEmail.setDisable(true);
        fieldTelephone.setDisable(true);
        fieldPoste.setDisable(true);
        fieldDepartement.setDisable(true);
        fieldDateEmbauche.setDisable(true);
        fieldSalaireBase.setDisable(true);
        fieldCin.setDisable(true);
        fieldAdresse.setDisable(true);
        comboStatut.setDisable(true);

        // Vider les labels d'erreur
        errNom.setText("");
        errPrenom.setText("");
        errEmail.setText("");
        errTelephone.setText("");
        errAdresse.setText("");

        labelMessage.setText("");
        labelMessage.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
    }

    // ════════════════════════ MESSAGES GLOBAUX ════════════════════════════════
    private void succes(String msg) {
        labelMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; "
            + "-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        labelMessage.setText(msg);
    }

    private void erreur(String msg) {
        labelMessage.setStyle("-fx-text-fill: #EF4444; "
            + "-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        labelMessage.setText(msg);
    }
}