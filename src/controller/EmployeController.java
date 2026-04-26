package controller;

import dao.EmployeDAO;
import model.Employe;
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

    // ─── RECHERCHE & MESSAGES ───────────────────────────────────────────────
    @FXML private TextField    fieldRecherche;
    @FXML private Label        labelMessage;
    @FXML private Label        labelNbEmployes;

    private final EmployeDAO employeDAO = new EmployeDAO();
    private ObservableList<Employe> listeEmployes;
    private FilteredList<Employe>   listeFiltree;

    // ════════════════════════ INIT ════════════════════════════════════════════
    @FXML
    public void initialize() {

        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDateEmbauche.setCellValueFactory(new PropertyValueFactory<>("dateEmbauche"));
        colSalaireBase.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));

        // Colonne statut avec couleur
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

        // Colonne ancienneté
        colAnciennete.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getAnciennete() + " ans"));

        // ComboBox statut
        comboStatut.setItems(FXCollections.observableArrayList(Employe.Statut.values()));
        comboStatut.setValue(Employe.Statut.ACTIF);

        // Data
        listeEmployes = FXCollections.observableArrayList();
        listeFiltree  = new FilteredList<>(listeEmployes, p -> true);
        tableEmployes.setItems(listeFiltree);

        // Recherche en temps réel
        fieldRecherche.textProperty().addListener((obs, ancienne, nouvelle) -> filtrer(nouvelle));

        // Sélection → remplir formulaire
        tableEmployes.getSelectionModel().selectedItemProperty().addListener(
            (obs, ancien, nouveau) -> remplirFormulaire(nouveau));

        chargerEmployes();
    }

    // ════════════════════════ CHARGEMENT ══════════════════════════════════════
    private void chargerEmployes() {
        listeEmployes.setAll(employeDAO.getTous());
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
        if (!validerChamps()) return;
        try {
            Employe e = construireDepuisForm(0);
            if (employeDAO.ajouter(e)) {
                chargerEmployes();
                viderChamps();
                succes("✔ Employé ajouté avec succès.");
            } else {
                erreur("❌ Échec de l'ajout. Vérifiez la base de données.");
            }
        } catch (Exception ex) {
            erreur("❌ Erreur : " + ex.getMessage());
        }
    }

    // ════════════════════════ MODIFIER ════════════════════════════════════════
    @FXML
    private void modifierEmploye() {
        Employe selectionne = tableEmployes.getSelectionModel().getSelectedItem();
        if (selectionne == null) { erreur("⚠ Sélectionnez un employé à modifier."); return; }
        if (!validerChamps()) return;
        try {
            Employe e = construireDepuisForm(selectionne.getId());
            if (employeDAO.modifier(e)) {
                chargerEmployes();
                succes("✔ Employé modifié avec succès.");
            } else {
                erreur("❌ Échec de la modification.");
            }
        } catch (Exception ex) {
            erreur("❌ Erreur : " + ex.getMessage());
        }
    }

    // ════════════════════════ SUPPRIMER ═══════════════════════════════════════
    @FXML
    private void supprimerEmploye() {
        Employe selectionne = tableEmployes.getSelectionModel().getSelectedItem();
        if (selectionne == null) { erreur("⚠ Sélectionnez un employé."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + selectionne.getNomComplet() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                if (employeDAO.supprimer(selectionne.getId())) {
                    chargerEmployes();
                    viderChamps();
                    succes("✔ Employé supprimé.");
                } else {
                    erreur("❌ Suppression impossible (peut-être lié à des salaires/congés).");
                }
            }
        });
    }

    // ════════════════════════ HELPERS FORMULAIRE ══════════════════════════════
    private Employe construireDepuisForm(int id) {
        return new Employe(
            id,
            fieldNom.getText().trim(),
            fieldPrenom.getText().trim(),
            fieldEmail.getText().trim(),
            fieldTelephone.getText().trim(),
            fieldPoste.getText().trim(),
            fieldDepartement.getText().trim(),
            fieldDateEmbauche.getValue().toString(),
            Double.parseDouble(fieldSalaireBase.getText().trim()),
            fieldCin.getText().trim(),
            fieldAdresse.getText().trim(),
            comboStatut.getValue()
        );
    }

    private void remplirFormulaire(Employe e) {
        if (e == null) return;
        fieldNom.setText(e.getNom());
        fieldPrenom.setText(e.getPrenom());
        fieldEmail.setText(e.getEmail());
        fieldTelephone.setText(e.getTelephone());
        fieldPoste.setText(e.getPoste());
        fieldDepartement.setText(e.getDepartement());
        try { fieldDateEmbauche.setValue(LocalDate.parse(e.getDateEmbauche())); } catch (Exception ignored) {}
        fieldSalaireBase.setText(String.valueOf(e.getSalaireBase()));
        fieldCin.setText(e.getCin());
        fieldAdresse.setText(e.getAdresse());
        comboStatut.setValue(e.getStatut());
        labelMessage.setText("");
    }

    @FXML
    private void viderChamps() {
        fieldNom.clear(); fieldPrenom.clear(); fieldEmail.clear();
        fieldTelephone.clear(); fieldPoste.clear(); fieldDepartement.clear();
        fieldDateEmbauche.setValue(null); fieldSalaireBase.clear();
        fieldCin.clear(); fieldAdresse.clear();
        comboStatut.setValue(Employe.Statut.ACTIF);
        fieldRecherche.clear();
        tableEmployes.getSelectionModel().clearSelection();
        labelMessage.setText("");
    }

    // ════════════════════════ VALIDATION ══════════════════════════════════════
    private boolean validerChamps() {
        StringBuilder sb = new StringBuilder();
        if (fieldNom.getText().trim().isEmpty())    sb.append("• Nom requis\n");
        if (fieldPrenom.getText().trim().isEmpty()) sb.append("• Prénom requis\n");
        if (fieldPoste.getText().trim().isEmpty())  sb.append("• Poste requis\n");
        if (fieldDateEmbauche.getValue() == null)   sb.append("• Date d'embauche requise\n");
        if (fieldSalaireBase.getText().trim().isEmpty()) {
            sb.append("• Salaire base requis\n");
        } else {
            try { Double.parseDouble(fieldSalaireBase.getText().trim()); }
            catch (NumberFormatException ex) { sb.append("• Salaire invalide (chiffres seulement)\n"); }
        }
        if (!fieldEmail.getText().trim().isEmpty() &&
            !fieldEmail.getText().contains("@")) {
            sb.append("• Email invalide\n");
        }
        if (sb.length() > 0) { erreur("⚠ Erreurs :\n" + sb); return false; }
        return true;
    }

    // ════════════════════════ MESSAGES ════════════════════════════════════════
    private void succes(String msg) {
        labelMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        labelMessage.setText(msg);
    }
    private void erreur(String msg) {
        labelMessage.setStyle("-fx-text-fill: #EF4444;");
        labelMessage.setText(msg);
    }
}
